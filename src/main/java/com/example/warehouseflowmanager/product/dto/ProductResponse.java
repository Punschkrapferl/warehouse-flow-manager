package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ProductResponse",
        description = "Product details returned by the API."
)
public class ProductResponse {

    @Schema(
            description = "Unique identifier of the product.",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Unique stock keeping unit of the product.",
            example = "SKU-1001"
    )
    private String sku;

    @Schema(
            description = "Display name of the product.",
            example = "Industrial Storage Bin"
    )
    private String name;

    @Schema(
            description = "Optional product description.",
            example = "Large plastic bin for warehouse spare parts",
            nullable = true
    )
    private String description;

    @Schema(
            description = "Unit of measurement used for the product.",
            example = "piece"
    )
    private String unit;

    @Schema(
            description = "Current available quantity in stock.",
            example = "120",
            minimum = "0"
    )
    private Integer quantity;

    @Schema(
            description = "ID of the assigned storage location. Null if the product is not assigned to a storage location.",
            example = "2",
            nullable = true
    )
    private Long storageLocationId;

    @Schema(
            description = "Code of the assigned storage location. Null if the product is not assigned to a storage location.",
            example = "A-01-01",
            nullable = true
    )
    private String storageLocationCode;

    @Schema(
            description = "Current lifecycle status of the product.",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "BLOCKED", "DISCONTINUED"}
    )
    private ProductStatus status;

    @Schema(
            description = "Minimum quantity threshold below which the product is considered low stock.",
            example = "25",
            minimum = "0"
    )
    private Integer minimumQuantity;

    @Schema(
            description = "Indicates whether the current quantity is at or below the minimum quantity threshold.",
            example = "false"
    )
    private boolean lowStock;
}