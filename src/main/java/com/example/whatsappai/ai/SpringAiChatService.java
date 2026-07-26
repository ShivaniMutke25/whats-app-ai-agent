package com.example.whatsappai.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class SpringAiChatService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiChatService.class);
    private final ChatModel chatModel;

    public SpringAiChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String generateResponse(String systemPrompt, String userPrompt) {
        Prompt prompt = new Prompt(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        );

        var response = chatModel.call(prompt);
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            log.warn("Spring AI returned empty response");
            return "I’m sorry, I couldn’t generate a response right now.";
        }

        AssistantMessage assistantMessage = response.getResult().getOutput();
        return assistantMessage.getText();
    }
}
