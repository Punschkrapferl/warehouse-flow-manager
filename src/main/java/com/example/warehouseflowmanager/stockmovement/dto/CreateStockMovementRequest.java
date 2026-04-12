package com.example.warehouseflowmanager.stockmovement.dto;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateStockMovementRequest {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be greater than 0")
    private Long productId;

    @NotNull(message = "Movement type is required")
    private StockMovementType movementType;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or greater")
    private Integer quantity;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}