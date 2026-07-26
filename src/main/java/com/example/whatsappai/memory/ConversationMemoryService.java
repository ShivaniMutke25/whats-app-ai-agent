package com.example.whatsappai.memory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationMemoryService {

    private final Map<String, List<ConversationTurn>> memory = new LinkedHashMap<>();

    public List<ConversationTurn> load(String customerId) {
        return memory.getOrDefault(customerId, List.of());
    }

    public void save(String customerId, String userMessage, String assistantResponse) {
        List<ConversationTurn> turns = memory.computeIfAbsent(customerId, ignored -> new ArrayList<>());
        turns.add(new ConversationTurn("user", userMessage));
        turns.add(new ConversationTurn("assistant", assistantResponse));
        if (turns.size() > 6) {
            turns.subList(0, turns.size() - 6).clear();
        }
    }
}
