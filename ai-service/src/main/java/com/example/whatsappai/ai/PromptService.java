package com.example.whatsappai.ai;

import com.example.whatsappai.memory.ConversationTurn;
import com.example.whatsappai.rag.RetrievedDocument;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptService {

    public String buildPrompt(String customerMessage, List<ConversationTurn> history, List<RetrievedDocument> retrievedDocuments, String businessContext) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a helpful support assistant for a WhatsApp customer service prototype serving small shopkeepers.\n");
        builder.append("Use the available context and customer information when it is relevant. Do not invent policies or product availability.\n");
        builder.append("If the answer cannot be confirmed from the available context, say so clearly and offer a safe alternative.\n\n");

        if (businessContext != null && !businessContext.isBlank()) {
            builder.append("Business context:\n");
            builder.append(businessContext).append("\n\n");
        }

        if (!history.isEmpty()) {
            builder.append("Conversation history:\n");
            for (ConversationTurn turn : history) {
                builder.append(turn.role()).append(": ").append(turn.content()).append("\n");
            }
            builder.append("\n");
        }

        if (!retrievedDocuments.isEmpty()) {
            builder.append("Retrieved context:\n");
            for (RetrievedDocument document : retrievedDocuments) {
                builder.append("- ").append(document.content()).append("\n");
            }
            builder.append("\n");
        }

        builder.append("Customer message: ").append(customerMessage);
        return builder.toString();
    }
}
