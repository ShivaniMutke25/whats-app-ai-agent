package com.example.whatsappai.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationIdUtil {

    private CorrelationIdUtil() {
    }

    public static String generate() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        return correlationId;
    }

    public static String nextId() {
        return generate();
    }
}
