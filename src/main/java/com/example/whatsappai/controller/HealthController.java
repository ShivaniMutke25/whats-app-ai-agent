package com.example.whatsappai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "WhatsApp AI Agent is up";
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "whatsapp-ai-agent");
    }

    public record HealthResponse(String status, String service) {
    }
}
