package com.example.warehouseflowmanager.stockmovement.dto;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "StockMovementResponse",
        description = "Stock movement details returned by the API."
)
public class StockMovementResponse {

    @Schema(
            description = "Unique identifier of the stock movement.",
            example = "15"
    )
    private Long id;

    @Schema(
            description = "ID of the product affected by this stock movement.",
            example = "1"
    )
    private Long productId;

    @Schema(
            description = "SKU of the affected product.",
            example = "SKU-1001"
    )
    private String productSku;

    @Schema(
            description = "Type of stock movement that was performed.",
            example = "INBOUND"
    )
    private StockMovementType movementType;

    @Schema(
            description = "Quantity involved in the stock movement.",
            example = "50"
    )
    private Integer quantity;

    @Schema(
            description = "Product stock quantity after the movement was applied.",
            example = "170"
    )
    private Integer resultingQuantity;

    @Schema(
            description = "Optional note explaining the reason for the stock movement.",
            example = "Initial stock on product creation",
            nullable = true
    )
    private String note;

    @Schema(
            description = "Timestamp when the stock movement was recorded.",
            example = "2026-04-12T19:45:00Z"
    )
    private Instant movementAt;
}