package com.example.whatsappai.ai;

import com.example.whatsappai.memory.ConversationTurn;
import com.example.whatsappai.rag.RetrievedDocument;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptService {

    public String buildPrompt(String customerMessage, List<ConversationTurn> history, List<RetrievedDocument> retrievedDocuments) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a helpful support assistant for a WhatsApp customer service prototype.\n");
        builder.append("Use the retrieved context when it is relevant. Do not invent policies.\n");
        builder.append("If the answer cannot be confirmed from the available context, say so clearly.\n\n");

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
