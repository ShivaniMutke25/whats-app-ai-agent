package com.example.whatsappai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiClient(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public AgentResponse generateResponse(String prompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return fallbackResponse(prompt);
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", properties.getModel(),
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "You are a helpful customer support assistant."),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", properties.getTemperature()
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("OpenAI request failed with status {}", response.statusCode());
                return fallbackResponse(prompt);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String answer = root.path("choices").path(0).path("message").path("content").asText("")
                    .replace("\n", " ").trim();
            if (answer.isBlank()) {
                return fallbackResponse(prompt);
            }

            return new AgentResponse(answer, "GENERAL", 0.8, false);
        } catch (Exception ex) {
            log.warn("OpenAI integration failed, using fallback response: {}", ex.getMessage());
            return fallbackResponse(prompt);
        }
    }

    private AgentResponse fallbackResponse(String prompt) {
        String normalized = prompt == null ? "" : prompt.toLowerCase();
        if (normalized.contains("order")) {
            return new AgentResponse("I can help with your order. Please share your order ID if you want a status update.", "ORDER", 0.75, false);
        }
        if (normalized.contains("refund") || normalized.contains("return")) {
            return new AgentResponse("I can help with refunds and returns. Please share the order details and I will guide you.", "REFUND", 0.74, false);
        }
        return new AgentResponse("Thanks for contacting support. I can help with orders, deliveries, payments, and refunds.", "GENERAL", 0.7, false);
    }
}
