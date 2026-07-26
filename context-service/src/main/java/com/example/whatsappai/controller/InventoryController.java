package com.example.whatsappai.controller;

import com.example.whatsappai.domain.InventoryItem;
import com.example.whatsappai.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Collection<InventoryItem> list() {
        return inventoryService.listItems();
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItem> get(@PathVariable String itemId) {
        return inventoryService.findById(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InventoryItem> create(@RequestBody @Valid InventoryItem item) {
        InventoryItem saved = inventoryService.save(item);
        return ResponseEntity.created(URI.create("/inventory/" + saved.itemId())).body(saved);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<InventoryItem> update(@PathVariable String itemId,
                                                @RequestBody @Valid InventoryItem item) {
        InventoryItem updated = new InventoryItem(itemId, item.sku(), item.name(), item.description(), item.quantity(), item.price());
        return ResponseEntity.ok(inventoryService.save(updated));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable String itemId) {
        inventoryService.delete(itemId);
        return ResponseEntity.noContent().build();
    }
}
