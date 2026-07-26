package com.example.whatsappai.service;

import com.example.whatsappai.domain.InventoryItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    private final Map<String, InventoryItem> inventory = new ConcurrentHashMap<>();

    public InventoryService() {
        inventory.put("ITEM-001", new InventoryItem("ITEM-001", "SKU-001", "Shipping Box", "Standard shipping box", 120, new BigDecimal("4.50")));
        inventory.put("ITEM-002", new InventoryItem("ITEM-002", "SKU-002", "Receipt Book", "60-page receipt book", 40, new BigDecimal("9.99")));
        inventory.put("ITEM-003", new InventoryItem("ITEM-003", "SKU-003", "Label Roll", "Thermal label roll", 75, new BigDecimal("12.50")));
    }

    public Collection<InventoryItem> listItems() {
        return inventory.values();
    }

    public Optional<InventoryItem> findById(String itemId) {
        return Optional.ofNullable(inventory.get(itemId));
    }

    public InventoryItem save(InventoryItem item) {
        inventory.put(item.itemId(), item);
        return item;
    }

    public void delete(String itemId) {
        inventory.remove(itemId);
    }
}
