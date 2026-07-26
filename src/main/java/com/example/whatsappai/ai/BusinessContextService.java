package com.example.whatsappai.ai;

import com.example.whatsappai.domain.CustomerProfile;
import com.example.whatsappai.domain.InventoryItem;
import com.example.whatsappai.service.CustomerService;
import com.example.whatsappai.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BusinessContextService {

    private final InventoryService inventoryService;
    private final CustomerService customerService;

    public BusinessContextService(InventoryService inventoryService, CustomerService customerService) {
        this.inventoryService = inventoryService;
        this.customerService = customerService;
    }

    public String buildBusinessContext(String customerId, String userMessage) {
        StringBuilder builder = new StringBuilder();
        if (customerId != null && !customerId.isBlank()) {
            Optional<CustomerProfile> customer = customerService.findById(customerId);
            customer.ifPresent(profile -> builder.append("Customer profile:\n")
                    .append("- Name: ").append(profile.name()).append("\n")
                    .append("- Email: ").append(profile.email()).append("\n")
                    .append("- Phone: ").append(profile.phoneNumber()).append("\n")
                    .append("- Notes: ").append(profile.notes()).append("\n\n"));
        }

        if (userMessage != null && userMessage.toLowerCase().contains("inventory")) {
            builder.append("Inventory context:\n");
            for (InventoryItem item : inventoryService.listItems()) {
                builder.append("- ")
                        .append(item.itemId())
                        .append(" | ")
                        .append(item.name())
                        .append(" | qty: ")
                        .append(item.quantity())
                        .append(" | price: ")
                        .append(item.price())
                        .append("\n");
            }
        }

        return builder.toString().trim();
    }
}
