package com.example.whatsappai.service;

import com.example.whatsappai.domain.CustomerProfile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerService {

    private final Map<String, CustomerProfile> customers = new ConcurrentHashMap<>();

    public CustomerService() {
        customers.put("CUST-001", new CustomerProfile("CUST-001", "Chandan Retail", "chandan@example.com", "+919876543210", "Preferred payment by UPI"));
        customers.put("CUST-002", new CustomerProfile("CUST-002", "Sunita Store", "sunita@example.com", "+919812345678", "Frequent buyer of stationery"));
    }

    public Collection<CustomerProfile> listCustomers() {
        return customers.values();
    }

    public Optional<CustomerProfile> findById(String customerId) {
        return Optional.ofNullable(customers.get(customerId));
    }

    public CustomerProfile save(CustomerProfile profile) {
        customers.put(profile.customerId(), profile);
        return profile;
    }

    public void delete(String customerId) {
        customers.remove(customerId);
    }
}
