package com.example.whatsappai.controller;

import com.example.whatsappai.service.HealthCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/")
    public String home() {
        return "WhatsApp AI Agent is up";
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(healthCheckService.getStatus(), "whatsapp-ai-agent");
    }

    public record HealthResponse(String status, String service) {
    }
}
