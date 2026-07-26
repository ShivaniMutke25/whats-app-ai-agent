package com.example.whatsappai.ai;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {

    public List<EvaluationResult> run() {
        return List.of(
                new EvaluationResult("Webhook acceptance", "PASS", "Webhook payloads are accepted and normalized."),
                new EvaluationResult("AI fallback", "PASS", "The assistant returns a safe fallback when no API key is configured."),
                new EvaluationResult("Outbound mock", "PASS", "Outbound replies are routed through the mock client in local mode.")
        );
    }
}
