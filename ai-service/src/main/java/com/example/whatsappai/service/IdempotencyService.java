package com.example.whatsappai.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private final Set<String> claimedMessageIds = ConcurrentHashMap.newKeySet();

    public boolean claim(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return false;
        }
        return claimedMessageIds.add(messageId);
    }
}
