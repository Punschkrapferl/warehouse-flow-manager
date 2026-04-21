package com.example.warehouseflowmanager.product.dto;

public record ReplenishmentRecommendationResponse(
        Long productId,
        String sku,
        String name,
        String unit,
        Integer currentQuantity,
        Integer minimumQuantity,
        Integer shortageQuantity,
        Integer recentOutboundQuantity,
        Integer recommendedReorderQuantity,
        String priority,
        Long storageLocationId,
        String storageLocationCode
) {
}