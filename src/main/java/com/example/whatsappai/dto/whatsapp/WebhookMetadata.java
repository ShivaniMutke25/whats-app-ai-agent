package com.example.whatsappai.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookMetadata(
        @NotBlank String display_phone_number,
        @NotBlank String phone_number_id
) {
}
