package com.example.warehouseflowmanager.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RelocateProductRequest(
        @NotNull(message = "Storage location ID is required")
        @Positive(message = "Storage location ID must be greater than 0")
        Long storageLocationId
) {
}