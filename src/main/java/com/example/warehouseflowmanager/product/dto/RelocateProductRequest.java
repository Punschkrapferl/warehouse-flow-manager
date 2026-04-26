package com.example.warehouseflowmanager.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request payload for relocating a product to another storage location")
public record RelocateProductRequest(

        @NotNull(message = "Storage location ID is required")
        @Positive(message = "Storage location ID must be greater than 0")
        @Schema(
                description = "Target storage location ID",
                example = "2",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long storageLocationId
) {
}