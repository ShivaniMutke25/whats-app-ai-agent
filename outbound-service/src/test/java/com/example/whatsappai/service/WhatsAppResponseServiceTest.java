package com.example.whatsappai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class WhatsAppResponseServiceTest {

    @Test
    void sendReplyInMockModeDoesNotThrow() {
        WhatsAppResponseService service = new WhatsAppResponseService(new MockWhatsAppClient());

        assertDoesNotThrow(() -> service.sendReply("+919876543210", "Hello from test"));
    }
}
