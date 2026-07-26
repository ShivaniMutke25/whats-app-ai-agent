package com.example.whatsappai.ai;

import org.springframework.stereotype.Service;

@Service
public class StructuredResponseService {

    public AgentResponse normalize(String answer, String category) {
        String normalizedAnswer = answer == null ? "" : answer.trim();
        if (normalizedAnswer.isBlank()) {
            normalizedAnswer = "Thanks for contacting support. I can help with orders, deliveries, payments, and refunds.";
        }
        String normalizedCategory = category == null || category.isBlank() ? "GENERAL" : category;
        return new AgentResponse(normalizedAnswer, normalizedCategory, 0.8, false);
    }
}
