package com.example.warehouseflowmanager.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @NotBlank(message = "SKU must not be blank")
        @Size(max = 50, message = "SKU must not be longer than 50 characters")
        String sku,

        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not be longer than 255 characters")
        String name,

        @Size(max = 1000, message = "Description must not be longer than 1000 characters")
        String description,

        @NotBlank(message = "Unit must not be blank")
        @Size(max = 30, message = "Unit must not be longer than 30 characters")
        String unit,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be 0 or greater")
        Integer quantity,

        @NotBlank(message = "Location code must not be blank")
        @Size(max = 50, message = "Location code must not be longer than 50 characters")
        String locationCode
) {
}