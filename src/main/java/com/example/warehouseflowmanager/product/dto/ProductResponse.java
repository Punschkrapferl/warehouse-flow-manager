package com.example.warehouseflowmanager.product.dto;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String unit,
        Integer quantity
) {
}