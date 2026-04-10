package com.example.warehouseflowmanager.stockmovement.dto;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;

import java.time.Instant;

public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productSku;
    private StockMovementType movementType;
    private Integer quantity;
    private Integer resultingQuantity;
    private String note;
    private Instant movementAt;

    public StockMovementResponse() {
    }

    public StockMovementResponse(
            Long id,
            Long productId,
            String productSku,
            StockMovementType movementType,
            Integer quantity,
            Integer resultingQuantity,
            String note,
            Instant movementAt
    ) {
        this.id = id;
        this.productId = productId;
        this.productSku = productSku;
        this.movementType = movementType;
        this.quantity = quantity;
        this.resultingQuantity = resultingQuantity;
        this.note = note;
        this.movementAt = movementAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getResultingQuantity() {
        return resultingQuantity;
    }

    public String getNote() {
        return note;
    }

    public Instant getMovementAt() {
        return movementAt;
    }
}