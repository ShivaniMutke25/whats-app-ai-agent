package com.example.whatsappai.ai;

import com.example.whatsappai.memory.ConversationTurn;
import com.example.whatsappai.rag.RetrievedDocument;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContextBuilder {

    public String buildPrompt(String customerMessage, List<ConversationTurn> history, List<RetrievedDocument> retrievedDocuments) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a concise and helpful customer support assistant.\n");
        builder.append("Answer only with information supported by the provided context or your general knowledge.\n");

        if (!history.isEmpty()) {
            builder.append("\nConversation history:\n");
            for (ConversationTurn turn : history) {
                builder.append(turn.role()).append(": ").append(turn.content()).append("\n");
            }
        }

        if (!retrievedDocuments.isEmpty()) {
            builder.append("\nRetrieved context:\n");
            for (RetrievedDocument document : retrievedDocuments) {
                builder.append("- ").append(document.content()).append("\n");
            }
        }

        builder.append("\nCustomer message: ").append(customerMessage);
        return builder.toString();
    }
}
