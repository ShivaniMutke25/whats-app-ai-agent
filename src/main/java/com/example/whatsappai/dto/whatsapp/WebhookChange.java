package com.example.whatsappai.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookChange(
        @NotNull @Valid WebhookValue value
) {
}
