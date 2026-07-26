package com.example.whatsappai.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageText(
        @NotBlank String body
) {
}
