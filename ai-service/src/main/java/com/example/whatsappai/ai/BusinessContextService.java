package com.example.whatsappai.ai;

import com.example.whatsappai.domain.CustomerProfile;
import com.example.whatsappai.domain.InventoryItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BusinessContextService {
    private final RestClient contextClient;

    public BusinessContextService(@Value("${app.context-service.base-url:http://context-service:8082}") String contextServiceUrl) {
        this.contextClient = RestClient.builder().baseUrl(contextServiceUrl).build();
    }

    public String buildBusinessContext(String customerId, String userMessage) {
        StringBuilder context = new StringBuilder();
        try {
            if (customerId != null && !customerId.isBlank()) {
                CustomerProfile customer = contextClient.get().uri("/customers/{id}", customerId)
                        .retrieve().body(CustomerProfile.class);
                if (customer != null) context.append("Customer: ").append(customer.name()).append("; notes: ").append(customer.notes()).append("\n");
            }
            if (userMessage != null && userMessage.toLowerCase().contains("inventory")) {
                List<InventoryItem> items = contextClient.get().uri("/inventory").retrieve()
                        .body(new ParameterizedTypeReference<List<InventoryItem>>() {});
                if (items != null) for (InventoryItem item : items) context.append("Inventory: ").append(item.name()).append(" qty=").append(item.quantity()).append(" price=").append(item.price()).append("\n");
            }
        } catch (Exception ignored) {
            context.append("Business context is temporarily unavailable.\n");
        }
        return context.toString().trim();
    }
}
