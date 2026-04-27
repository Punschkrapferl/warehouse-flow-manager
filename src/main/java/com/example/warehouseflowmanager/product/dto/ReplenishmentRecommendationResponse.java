package com.example.warehouseflowmanager.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ReplenishmentRecommendationResponse",
        description = "Recommended product replenishment information based on stock shortage and recent outbound demand."
)
public record ReplenishmentRecommendationResponse(

        @Schema(
                description = "Unique identifier of the product.",
                example = "1"
        )
        Long productId,

        @Schema(
                description = "Unique stock keeping unit of the product.",
                example = "SKU-1001"
        )
        String sku,

        @Schema(
                description = "Display name of the product.",
                example = "Industrial Storage Bin"
        )
        String name,

        @Schema(
                description = "Unit of measurement used for the product.",
                example = "piece"
        )
        String unit,

        @Schema(
                description = "Current available quantity in stock.",
                example = "8"
        )
        Integer currentQuantity,

        @Schema(
                description = "Minimum quantity threshold configured for the product.",
                example = "25"
        )
        Integer minimumQuantity,

        @Schema(
                description = "Current shortage compared with the minimum quantity threshold.",
                example = "17"
        )
        Integer shortageQuantity,

        @Schema(
                description = "Quantity removed through outbound movements during the selected recent period.",
                example = "40"
        )
        Integer recentOutboundQuantity,

        @Schema(
                description = "Recommended reorder quantity based on current shortage and recent outbound demand.",
                example = "57"
        )
        Integer recommendedReorderQuantity,

        @Schema(
                description = "Business priority of the replenishment recommendation.",
                example = "HIGH",
                allowableValues = {"HIGH", "MEDIUM", "LOW"}
        )
        String priority,

        @Schema(
                description = "ID of the assigned storage location. Null if the product is not assigned to a storage location.",
                example = "2",
                nullable = true
        )
        Long storageLocationId,

        @Schema(
                description = "Code of the assigned storage location. Null if the product is not assigned to a storage location.",
                example = "A-01-01",
                nullable = true
        )
        String storageLocationCode
) {
}