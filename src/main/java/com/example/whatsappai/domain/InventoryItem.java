package com.example.whatsappai.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventoryItem(
        @NotBlank String itemId,
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(0) Integer quantity,
        @NotNull BigDecimal price
) {
}
