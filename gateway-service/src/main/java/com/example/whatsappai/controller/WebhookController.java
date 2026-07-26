package com.example.whatsappai.controller;

import com.example.whatsappai.domain.WhatsAppMessage;
import com.example.whatsappai.dto.whatsapp.WebhookRequest;
import com.example.whatsappai.kafka.WhatsAppMessageEvent;
import com.example.whatsappai.kafka.producer.InboundMessageProducer;
import com.example.whatsappai.service.IdempotencyService;
import com.example.whatsappai.service.InboundMessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/whatsapp")
@Validated
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final InboundMessageService inboundMessageService;
    private final InboundMessageProducer inboundMessageProducer;
    private final IdempotencyService idempotencyService;

    public WebhookController(InboundMessageService inboundMessageService,
                             InboundMessageProducer inboundMessageProducer,
                             IdempotencyService idempotencyService) {
        this.inboundMessageService = inboundMessageService;
        this.inboundMessageProducer = inboundMessageProducer;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String verifyToken) {

        if ("subscribe".equals(mode) && "local-verify-token".equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@Valid @RequestBody WebhookRequest request) {
        WhatsAppMessage message = inboundMessageService.normalize(request);

        if (!idempotencyService.claim(message.messageId())) {
            log.info("Duplicate webhook ignored for messageId={}", message.messageId());
            return ResponseEntity.accepted().body(Map.of(
                    "status", "duplicate",
                    "messageId", message.messageId(),
                    "customerId", message.customerId(),
                    "correlationId", message.correlationId()
            ));
        }

        log.info("Accepted WhatsApp webhook messageId={} correlationId={}", message.messageId(), message.correlationId());

        var event = new WhatsAppMessageEvent(
                message.messageId() + "-event",
                message.messageId(),
                message.customerId(),
                message.phoneNumber(),
                message.message(),
                message.timestamp(),
                message.correlationId()
        );
        inboundMessageProducer.publish(event);

        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "messageId", message.messageId(),
                "customerId", message.customerId(),
                "correlationId", message.correlationId()
        ));
    }
}
