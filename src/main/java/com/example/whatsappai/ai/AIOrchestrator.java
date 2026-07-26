package com.example.whatsappai.ai;

import com.example.whatsappai.domain.WhatsAppMessage;
import com.example.whatsappai.guardrail.GuardrailService;
import com.example.whatsappai.guardrail.InputGuardrail;
import com.example.whatsappai.guardrail.OutputGuardrail;
import com.example.whatsappai.memory.ConversationMemoryService;
import com.example.whatsappai.rag.RagService;
import com.example.whatsappai.service.FallbackService;
import com.example.whatsappai.tools.OrderStatusTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AIOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AIOrchestrator.class);

    private final PromptService promptService;
    private final ConversationMemoryService conversationMemoryService;
    private final RagService ragService;
    private final OpenAiClient openAiClient;
    private final OrderStatusTool orderStatusTool;
    private final GuardrailService guardrailService;
    private final InputGuardrail inputGuardrail;
    private final OutputGuardrail outputGuardrail;
    private final FallbackService fallbackService;
    private final StructuredResponseService structuredResponseService;

    public AIOrchestrator(PromptService promptService,
                          ConversationMemoryService conversationMemoryService,
                          RagService ragService,
                          OpenAiClient openAiClient,
                          OrderStatusTool orderStatusTool,
                          GuardrailService guardrailService,
                          InputGuardrail inputGuardrail,
                          OutputGuardrail outputGuardrail,
                          FallbackService fallbackService,
                          StructuredResponseService structuredResponseService) {
        this.promptService = promptService;
        this.conversationMemoryService = conversationMemoryService;
        this.ragService = ragService;
        this.openAiClient = openAiClient;
        this.orderStatusTool = orderStatusTool;
        this.guardrailService = guardrailService;
        this.inputGuardrail = inputGuardrail;
        this.outputGuardrail = outputGuardrail;
        this.fallbackService = fallbackService;
        this.structuredResponseService = structuredResponseService;
    }

    public AgentResponse process(WhatsAppMessage message) {
        if (!inputGuardrail.isValid(message.message()) || !guardrailService.isSafe(message.message())) {
            return new AgentResponse(fallbackService.fallback(message.message()), "SAFE_GUARDRAIL", 0.0, true);
        }

        var history = conversationMemoryService.load(message.customerId());
        var retrievedDocuments = ragService.retrieve(message.message());
        var prompt = promptService.buildPrompt(message.message(), history, retrievedDocuments);

        if (message.message().toLowerCase().contains("order") && message.message().toLowerCase().contains("123")) {
            var toolResult = orderStatusTool.getStatus("ORD-123");
            log.info("Tool result for order lookup: {}", toolResult);
        }

        var response = openAiClient.generateResponse(prompt);
        var structuredResponse = structuredResponseService.normalize(response.answer(), response.category());
        var safeAnswer = outputGuardrail.sanitize(structuredResponse.answer());
        var safeResponse = new AgentResponse(safeAnswer, structuredResponse.category(), structuredResponse.confidence(), structuredResponse.requiresHumanSupport());
        conversationMemoryService.save(message.customerId(), message.message(), safeResponse.answer());
        return safeResponse;
    }
}
