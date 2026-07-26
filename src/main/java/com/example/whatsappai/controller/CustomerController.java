package com.example.whatsappai.controller;

import com.example.whatsappai.domain.CustomerProfile;
import com.example.whatsappai.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Collection<CustomerProfile> list() {
        return customerService.listCustomers();
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerProfile> get(@PathVariable String customerId) {
        return customerService.findById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerProfile> create(@RequestBody @Valid CustomerProfile profile) {
        CustomerProfile saved = customerService.save(profile);
        return ResponseEntity.created(URI.create("/customers/" + saved.customerId())).body(saved);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerProfile> update(@PathVariable String customerId,
                                                  @RequestBody @Valid CustomerProfile profile) {
        CustomerProfile updated = new CustomerProfile(customerId, profile.name(), profile.email(), profile.phoneNumber(), profile.notes());
        return ResponseEntity.ok(customerService.save(updated));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> delete(@PathVariable String customerId) {
        customerService.delete(customerId);
        return ResponseEntity.noContent().build();
    }
}
