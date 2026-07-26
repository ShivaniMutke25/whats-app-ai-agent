package com.example.whatsappai.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerProfile(
        @NotBlank String customerId,
        @NotBlank String name,
        @Email String email,
        @NotBlank String phoneNumber,
        String notes
) {
}
