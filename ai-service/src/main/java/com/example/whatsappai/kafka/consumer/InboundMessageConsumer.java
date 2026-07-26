package com.example.whatsappai.kafka.consumer;

import com.example.whatsappai.ai.AIOrchestrator;
import com.example.whatsappai.domain.WhatsAppMessage;
import com.example.whatsappai.kafka.WhatsAppMessageEvent;
import com.example.whatsappai.service.IdempotencyService;
import com.example.whatsappai.service.RateLimitService;
import com.example.whatsappai.service.OutboundServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InboundMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageConsumer.class);

    private final AIOrchestrator aiOrchestrator;
    private final OutboundServiceClient outboundServiceClient;
    private final IdempotencyService idempotencyService;
    private final RateLimitService rateLimitService;

    public InboundMessageConsumer(AIOrchestrator aiOrchestrator,
                                  OutboundServiceClient outboundServiceClient,
                                  IdempotencyService idempotencyService,
                                  RateLimitService rateLimitService) {
        this.aiOrchestrator = aiOrchestrator;
        this.outboundServiceClient = outboundServiceClient;
        this.idempotencyService = idempotencyService;
        this.rateLimitService = rateLimitService;
    }

    @KafkaListener(topics = "${app.kafka.topic.inbound:whatsapp.inbound}", groupId = "${app.kafka.consumer.group-id:whatsapp-ai-group}")
    public void receive(WhatsAppMessageEvent event) {
        if (event == null) {
            return;
        }

        if (!idempotencyService.claim(event.messageId())) {
            log.info("Duplicate inbound event ignored for messageId={}", event.messageId());
            return;
        }

        if (!rateLimitService.allow(event.customerId())) {
            log.warn("Rate limit exceeded for customerId={}", event.customerId());
            return;
        }

        log.info("Consumed inbound WhatsApp event messageId={} correlationId={}", event.messageId(), event.correlationId());

        var message = new WhatsAppMessage(
                event.messageId(),
                event.customerId(),
                event.phoneNumber(),
                event.message(),
                event.timestamp() != null ? event.timestamp() : Instant.now(),
                event.correlationId()
        );

        var response = aiOrchestrator.process(message);
        outboundServiceClient.sendReply(message.phoneNumber(), response.answer());
    }
}
