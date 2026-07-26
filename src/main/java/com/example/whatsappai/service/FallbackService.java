package com.example.whatsappai.service;

import org.springframework.stereotype.Service;

@Service
public class FallbackService {

    public String buildFallbackAnswer(String reason) {
        if (reason == null || reason.isBlank()) {
            return "I’m sorry, I’m unable to help right now. Please contact support for further assistance.";
        }
        return "I’m sorry, I’m unable to help with '" + reason + "' right now. Please contact support for further assistance.";
    }

    public String fallback(String message) {
        return buildFallbackAnswer(message);
    }
}
