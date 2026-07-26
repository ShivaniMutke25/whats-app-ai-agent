package com.example.whatsappai.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IncomingMessage(
        @NotBlank String from,
        @NotBlank String id,
        @NotNull Long timestamp,
        @NotBlank String type,
        @Valid MessageText text
) {
}
