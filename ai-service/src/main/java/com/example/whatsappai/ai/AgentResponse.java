package com.example.whatsappai.ai;

public record AgentResponse(
        String answer,
        String category,
        double confidence,
        boolean requiresHumanSupport
) {
}
