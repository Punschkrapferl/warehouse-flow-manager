package com.example.warehouseflowmanager.product.controller;

import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementSummaryResponse;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Product Stock Movements",
        description = "View stock movement history and stock movement summaries for a specific product"
)
public class ProductStockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping("/{id}/stock-movements")
    @Operation(
            summary = "Get stock movements for product",
            description = "Returns stock movements for a specific product with optional filtering by movement type and date range"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product stock movements retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID or invalid query parameters"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public List<StockMovementResponse> getProductStockMovements(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Product ID", example = "1")
            Long id,

            @RequestParam(required = false)
            @Parameter(description = "Optional movement type filter", example = "INBOUND")
            StockMovementType movementType,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "Optional start timestamp in ISO-8601 format",
                    example = "2026-04-12T08:00:00Z"
            )
            Instant from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "Optional end timestamp in ISO-8601 format",
                    example = "2026-04-12T18:00:00Z"
            )
            Instant to
    ) {
        return stockMovementService.getStockMovementsByProductId(id, movementType, from, to);
    }

    @GetMapping("/{id}/stock-movements/summary")
    @Operation(
            summary = "Get stock movement summary for product",
            description = "Returns aggregated stock movement totals for a specific product within an optional date range"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product stock movement summary retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID or invalid query parameters"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public StockMovementSummaryResponse getProductStockMovementSummary(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Product ID", example = "1")
            Long id,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "Optional start timestamp in ISO-8601 format",
                    example = "2026-04-12T08:00:00Z"
            )
            Instant from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "Optional end timestamp in ISO-8601 format",
                    example = "2026-04-12T18:00:00Z"
            )
            Instant to
    ) {
        return stockMovementService.getStockMovementSummaryByProductId(id, from, to);
    }
}