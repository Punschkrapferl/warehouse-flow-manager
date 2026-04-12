package com.example.warehouseflowmanager.storagelocation.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "StorageLocationStockItemResponse",
        description = "Product stock details contained within a storage location stock overview."
)
public class StorageLocationStockItemResponse {

    @Schema(
            description = "Unique identifier of the product.",
            example = "1"
    )
    private final Long id;

    @Schema(
            description = "Unique stock keeping unit of the product.",
            example = "SKU-1001"
    )
    private final String sku;

    @Schema(
            description = "Display name of the product.",
            example = "Industrial Storage Bin"
    )
    private final String name;

    @Schema(
            description = "Unit of measurement used for the product.",
            example = "piece"
    )
    private final String unit;

    @Schema(
            description = "Current quantity of the product in stock.",
            example = "120"
    )
    private final Integer quantity;

    @Schema(
            description = "Minimum quantity threshold below which the product is considered low stock.",
            example = "25"
    )
    private final Integer minimumQuantity;

    @Schema(
            description = "Current lifecycle status of the product.",
            example = "ACTIVE"
    )
    private final ProductStatus status;

    @Schema(
            description = "Indicates whether the product is currently at or below its minimum quantity threshold.",
            example = "false"
    )
    private final boolean lowStock;
}