package com.example.whatsappai.guardrail;

import org.springframework.stereotype.Service;

@Service
public class GuardrailService {

    public boolean isSafe(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = message.toLowerCase();
        return !normalized.contains("ignore previous instructions")
                && !normalized.contains("reveal system prompt")
                && !normalized.contains("show hidden instructions");
    }
}
