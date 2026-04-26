package com.example.warehouseflowmanager.stockmovement.dto;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a stock movement")
public class CreateStockMovementRequest {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be greater than 0")
    @Schema(
            description = "ID of the product affected by the stock movement",
            example = "1",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @NotNull(message = "Movement type is required")
    @Schema(
            description = "Type of stock movement",
            example = "INBOUND",
            allowableValues = {"INBOUND", "OUTBOUND", "ADJUSTMENT"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private StockMovementType movementType;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or greater")
    @Schema(
            description = "Movement quantity. INBOUND and OUTBOUND require a value greater than 0. ADJUSTMENT allows 0 or greater",
            example = "10",
            minimum = "0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer quantity;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    @Schema(
            description = "Optional note explaining the reason for the movement",
            example = "Restocked after supplier delivery",
            maxLength = 500,
            nullable = true
    )
    private String note;
}