package com.example.whatsappai.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public RateLimitService() {
        this(10, 60000L);
    }

    public RateLimitService(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    public boolean allow(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return false;
        }
        AtomicInteger counter = counters.computeIfAbsent(customerId, ignored -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        return current <= maxRequests;
    }
}
