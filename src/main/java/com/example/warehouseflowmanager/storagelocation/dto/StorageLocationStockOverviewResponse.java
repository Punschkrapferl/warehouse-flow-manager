package com.example.warehouseflowmanager.storagelocation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "StorageLocationStockOverviewResponse",
        description = "Stock overview for a storage location including aggregated totals and contained products."
)
public class StorageLocationStockOverviewResponse {

    @Schema(
            description = "Unique identifier of the storage location.",
            example = "1"
    )
    private final Long id;

    @Schema(
            description = "Human-readable storage location code.",
            example = "A-01-01"
    )
    private final String code;

    @Schema(
            description = "Warehouse zone of the storage location.",
            example = "ZONE-A"
    )
    private final String zone;

    @Schema(
            description = "Optional description of the storage location.",
            example = "Rack A, aisle 1, level 1"
    )
    private final String description;

    @Schema(
            description = "Indicates whether the storage location is active and available for use.",
            example = "true"
    )
    private final Boolean active;

    @Schema(
            description = "Number of products assigned to this storage location.",
            example = "8"
    )
    private final int totalProducts;

    @Schema(
            description = "Sum of all product quantities stored at this storage location.",
            example = "540"
    )
    private final int totalQuantity;

    @Schema(
            description = "Number of products at this storage location that are currently low in stock.",
            example = "2"
    )
    private final int lowStockProductCount;

    @ArraySchema(
            schema = @Schema(
                    implementation = StorageLocationStockItemResponse.class,
                    description = "Products currently assigned to this storage location."
            )
    )
    private final List<StorageLocationStockItemResponse> products;
}