package com.example.whatsappai.guardrail;

import org.springframework.stereotype.Service;

@Service
public class OutputGuardrail {

    public String sanitize(String answer) {
        if (answer == null || answer.isBlank()) {
            return "I can help with your request.";
        }
        return answer.trim();
    }
}
