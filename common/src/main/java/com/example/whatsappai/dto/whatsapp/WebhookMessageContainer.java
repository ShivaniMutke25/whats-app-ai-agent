package com.example.whatsappai.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookMessageContainer(
        @NotEmpty @Valid List<@Valid IncomingMessage> messages
) {
}
