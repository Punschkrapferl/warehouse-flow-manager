package com.example.warehouseflowmanager.stockmovement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "StockMovementSummaryResponse",
        description = "Aggregated stock movement summary for a product within an optional time range."
)
public class StockMovementSummaryResponse {

    @Schema(
            description = "ID of the product for which the summary was calculated.",
            example = "1"
    )
    private Long productId;

    @Schema(
            description = "SKU of the product for which the summary was calculated.",
            example = "SKU-1001"
    )
    private String productSku;

    @Schema(
            description = "Start of the requested time range. Null if no lower bound was provided.",
            example = "2026-04-01T00:00:00Z",
            nullable = true
    )
    private Instant from;

    @Schema(
            description = "End of the requested time range. Null if no upper bound was provided.",
            example = "2026-04-12T23:59:59Z",
            nullable = true
    )
    private Instant to;

    @Schema(
            description = "Total number of stock movements in the selected range.",
            example = "18"
    )
    private long totalMovements;

    @Schema(
            description = "Number of inbound stock movements in the selected range.",
            example = "7"
    )
    private long inboundMovementCount;

    @Schema(
            description = "Number of outbound stock movements in the selected range.",
            example = "6"
    )
    private long outboundMovementCount;

    @Schema(
            description = "Number of adjustment stock movements in the selected range.",
            example = "5"
    )
    private long adjustmentMovementCount;

    @Schema(
            description = "Total quantity added through inbound movements in the selected range.",
            example = "250"
    )
    private int totalInboundQuantity;

    @Schema(
            description = "Total quantity removed through outbound movements in the selected range.",
            example = "110"
    )
    private int totalOutboundQuantity;

    @Schema(
            description = "Net quantity affected by adjustment movements in the selected range.",
            example = "12"
    )
    private int totalAdjustmentQuantity;

    @Schema(
            description = "Current quantity of the product at the time the summary was generated.",
            example = "152",
            nullable = true
    )
    private Integer currentQuantity;

    @Schema(
            description = "Timestamp of the most recent stock movement for the product.",
            example = "2026-04-12T19:45:00Z",
            nullable = true
    )
    private Instant latestMovementAt;
}