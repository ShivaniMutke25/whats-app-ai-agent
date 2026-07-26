package com.example.whatsappai.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ConversationMemoryService {

    private static final Logger log = LoggerFactory.getLogger(ConversationMemoryService.class);
    private static final int MAX_TURNS = 6;
    private static final Duration MEMORY_TTL = Duration.ofHours(1);
    private static final String KEY_PREFIX = "whatsapp:conversation:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ConversationMemoryService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ConversationTurn> load(String customerId) {
        String cacheKey = key(customerId);
        String content = redisTemplate.opsForValue().get(cacheKey);
        if (content == null || content.isBlank()) {
            return List.of();
        }

        try {
            ConversationTurn[] turns = objectMapper.readValue(content, ConversationTurn[].class);
            return List.of(turns);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize conversation history for {}: {}", customerId, ex.getMessage());
            return List.of();
        }
    }

    public void save(String customerId, String userMessage, String assistantResponse) {
        List<ConversationTurn> existingTurns = new ArrayList<>(load(customerId));
        existingTurns.add(new ConversationTurn("user", userMessage));
        existingTurns.add(new ConversationTurn("assistant", assistantResponse));

        if (existingTurns.size() > MAX_TURNS) {
            existingTurns = existingTurns.subList(existingTurns.size() - MAX_TURNS, existingTurns.size());
        }

        try {
            String payload = objectMapper.writeValueAsString(existingTurns);
            redisTemplate.opsForValue().set(key(customerId), payload, MEMORY_TTL);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize conversation history for {}: {}", customerId, ex.getMessage());
        }
    }

    private String key(String customerId) {
        return KEY_PREFIX + customerId;
    }
}
