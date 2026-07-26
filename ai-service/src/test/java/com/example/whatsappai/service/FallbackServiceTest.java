package com.example.whatsappai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackServiceTest {

    @Test
    void returnsSafeFallbackForUnknownInput() {
        FallbackService service = new FallbackService();
        String answer = service.buildFallbackAnswer("unknown");

        assertTrue(answer.contains("support"));
    }
}
