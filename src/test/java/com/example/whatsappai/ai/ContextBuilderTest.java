package com.example.whatsappai.ai;

import com.example.whatsappai.memory.ConversationTurn;
import com.example.whatsappai.rag.RetrievedDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextBuilderTest {

    @Test
    void buildsPromptWithHistoryAndDocuments() {
        ContextBuilder builder = new ContextBuilder();
        String prompt = builder.buildPrompt(
                "I need a refund",
                List.of(new ConversationTurn("user", "Hello")),
                List.of(new RetrievedDocument("Refunds are allowed within 30 days.", 0.95))
        );

        assertTrue(prompt.contains("refund"));
        assertTrue(prompt.contains("Conversation history"));
        assertTrue(prompt.contains("Retrieved context"));
    }
}
