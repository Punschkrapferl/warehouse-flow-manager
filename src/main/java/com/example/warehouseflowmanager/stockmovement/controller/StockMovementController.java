package com.example.warehouseflowmanager.stockmovement.controller;

import com.example.warehouseflowmanager.stockmovement.dto.CreateStockMovementRequest;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementSummaryResponse;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Stock Movements",
        description = "Create and query inbound, outbound, and adjustment stock movements"
)
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create stock movement",
            description = """
                    Creates a stock movement for a product.
                    Supported movement types are INBOUND, OUTBOUND, and ADJUSTMENT.
                    Stock updates are handled through business rules and concurrency-safe persistence logic.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock movement created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or stock movement is not allowed"),
            @ApiResponse(responseCode = "404", description = "Referenced product not found")
    })
    public StockMovementResponse createStockMovement(
            @Valid
            @RequestBody
            @Parameter(description = "Stock movement creation payload")
            CreateStockMovementRequest request
    ) {
        return stockMovementService.createStockMovement(request);
    }

    @GetMapping
    @Operation(
            summary = "Get stock movements",
            description = "Returns stock movements with optional filtering by product, movement type, and date range"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more query parameters")
    })
    public List<StockMovementResponse> getStockMovements(
            @RequestParam(required = false)
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Optional product ID filter", example = "1")
            Long productId,

            @RequestParam(required = false)
            @Parameter(description = "Optional movement type filter")
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
        return stockMovementService.getStockMovements(productId, movementType, from, to);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get stock movement summary",
            description = "Returns aggregated stock movement totals for an optional product and optional date range"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movement summary retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more query parameters")
    })
    public StockMovementSummaryResponse getStockMovementSummary(
            @RequestParam(required = false)
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Optional product ID filter", example = "1")
            Long productId,

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
        return stockMovementService.getStockMovementSummary(productId, from, to);
    }
}