package com.example.whatsappai.controller;

import com.example.whatsappai.domain.CustomerProfile;
import com.example.whatsappai.domain.InventoryItem;
import com.example.whatsappai.service.CustomerService;
import com.example.whatsappai.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/shopkeeper")
public class ShopkeeperController {

    private final InventoryService inventoryService;
    private final CustomerService customerService;

    public ShopkeeperController(InventoryService inventoryService, CustomerService customerService) {
        this.inventoryService = inventoryService;
        this.customerService = customerService;
    }

    @GetMapping("/inventory")
    public Collection<InventoryItem> inventory() {
        return inventoryService.listItems();
    }

    @GetMapping("/inventory/{itemId}")
    public ResponseEntity<InventoryItem> item(@PathVariable String itemId) {
        return inventoryService.findById(itemId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customers")
    public Collection<CustomerProfile> customers() {
        return customerService.listCustomers();
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerProfile> customer(@PathVariable String customerId) {
        return customerService.findById(customerId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
