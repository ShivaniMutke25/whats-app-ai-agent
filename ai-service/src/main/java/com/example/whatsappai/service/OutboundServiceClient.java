package com.example.whatsappai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class OutboundServiceClient {
    private final RestClient client;
    public OutboundServiceClient(@Value("${app.outbound-service.base-url:http://outbound-service:8083}") String outboundServiceUrl) {
        this.client = RestClient.builder().baseUrl(outboundServiceUrl).build();
    }
    public void sendReply(String phoneNumber, String message) {
        client.post().uri("/internal/messages").body(Map.of("to", phoneNumber, "message", message)).retrieve().toBodilessEntity();
    }
}
