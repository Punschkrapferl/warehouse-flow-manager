package com.example.warehouseflowmanager.stockmovement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementSummaryResponse {

    private Long productId;
    private String productSku;
    private Instant from;
    private Instant to;
    private long totalMovements;
    private long inboundMovementCount;
    private long outboundMovementCount;
    private long adjustmentMovementCount;
    private int totalInboundQuantity;
    private int totalOutboundQuantity;
    private int totalAdjustmentQuantity;
    private Integer currentQuantity;
    private Instant latestMovementAt;
}