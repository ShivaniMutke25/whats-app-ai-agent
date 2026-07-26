package com.example.whatsappai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdempotencyServiceTest {

    @Test
    void secondClaimForSameMessageIsRejected() {
        IdempotencyService service = new IdempotencyService();

        assertTrue(service.claim("msg-1"));
        assertFalse(service.claim("msg-1"));
    }
}
