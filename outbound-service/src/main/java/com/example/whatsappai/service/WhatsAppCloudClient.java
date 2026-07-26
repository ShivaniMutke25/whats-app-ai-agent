package com.example.whatsappai.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class WhatsAppCloudClient implements WhatsAppClient {

    private final String accessToken;
    private final String phoneNumberId;
    private final String baseUrl;
    private final HttpClient httpClient;

    public WhatsAppCloudClient(String accessToken, String phoneNumberId, String baseUrl) {
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Override
    public void sendMessage(String toPhoneNumber, String message) {
        if (accessToken.isBlank() || phoneNumberId.isBlank()) {
            return;
        }

        try {
            String body = "{\"messaging_product\":\"whatsapp\",\"to\":\"" + toPhoneNumber + "\",\"type\":\"text\",\"text\":{\"body\":\"" + message.replace("\"", "\\\"") + "\"}}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/" + phoneNumberId + "/messages"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
            // Intentionally ignored for local/mock mode.
        }
    }
}
