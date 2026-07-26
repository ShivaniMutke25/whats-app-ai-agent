package com.example.whatsappai.domain;

import java.time.Instant;

public record WhatsAppMessage(
        String messageId,
        String customerId,
        String phoneNumber,
        String message,
        Instant timestamp,
        String correlationId
) {
}
