package com.example.whatsappai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppResponseService {

    private final WhatsAppClient client;

    public WhatsAppResponseService(WhatsAppClient client) {
        this.client = client;
    }

    public WhatsAppResponseService(@Value("${app.whatsapp.api.access-token:}") String accessToken,
                                   @Value("${app.whatsapp.phone-number-id:}") String phoneNumberId,
                                   @Value("${app.whatsapp.api.base-url:https://graph.facebook.com/v20.0}") String baseUrl) {
        this.client = (accessToken == null || accessToken.isBlank() || phoneNumberId == null || phoneNumberId.isBlank())
                ? new MockWhatsAppClient()
                : new WhatsAppCloudClient(accessToken, phoneNumberId, baseUrl);
    }

    public void sendReply(String toPhoneNumber, String message) {
        client.sendMessage(toPhoneNumber, message);
    }
}
