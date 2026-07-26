package com.example.whatsappai.kafka;

import java.time.Instant;

public record WhatsAppMessageEvent(
        String eventId,
        String messageId,
        String customerId,
        String phoneNumber,
        String message,
        Instant timestamp,
        String correlationId
) {
}
