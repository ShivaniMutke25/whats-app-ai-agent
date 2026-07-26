package com.example.whatsappai.tools;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderStatusTool {

    public Map<String, Object> getStatus(String orderId) {
        return Map.of(
                "orderId", orderId,
                "status", "Shipped",
                "estimatedDelivery", "3 business days"
        );
    }
}
