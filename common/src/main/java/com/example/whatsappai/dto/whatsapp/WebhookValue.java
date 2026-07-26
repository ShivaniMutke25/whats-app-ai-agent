package com.example.whatsappai.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookValue(
        @NotNull @Valid WebhookMetadata metadata,
        @Valid List<@Valid IncomingMessage> messages
) {
}
