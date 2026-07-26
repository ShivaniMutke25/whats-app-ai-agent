package com.example.whatsappai.service;

import com.example.whatsappai.domain.WhatsAppMessage;
import com.example.whatsappai.dto.whatsapp.IncomingMessage;
import com.example.whatsappai.dto.whatsapp.WebhookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class InboundMessageService {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageService.class);

    public WhatsAppMessage normalize(WebhookRequest request) {
        if (request == null || request.entry() == null || request.entry().isEmpty()) {
            throw new IllegalArgumentException("Webhook payload must contain at least one entry");
        }

        var firstEntry = request.entry().getFirst();
        var firstChange = firstEntry.changes().getFirst();
        var value = firstChange.value();

        if (value.messages() == null || value.messages().isEmpty()) {
            throw new IllegalArgumentException("Webhook payload must contain at least one message");
        }

        var incomingMessage = value.messages().getFirst();

        var correlationId = UUID.randomUUID().toString();
        var normalized = new WhatsAppMessage(
                incomingMessage.id(),
                incomingMessage.from(),
                value.metadata().display_phone_number(),
                incomingMessage.text() != null ? incomingMessage.text().body() : "",
                Instant.ofEpochSecond(incomingMessage.timestamp()),
                correlationId
        );

        log.info("Normalized inbound WhatsApp message messageId={} correlationId={}", normalized.messageId(), correlationId);
        return normalized;
    }
}
