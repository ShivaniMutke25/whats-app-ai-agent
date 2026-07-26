package com.example.whatsappai.service;

import org.springframework.stereotype.Service;

@Service
public class MockWhatsAppClient implements WhatsAppClient {

    @Override
    public void sendMessage(String toPhoneNumber, String message) {
        // Mock implementation for local/test mode.
    }
}
