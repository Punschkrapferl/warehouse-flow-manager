package com.example.warehouseflowmanager.stockmovement.controller;

import com.example.warehouseflowmanager.stockmovement.dto.CreateStockMovementRequest;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementSummaryResponse;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse createStockMovement(
            @Valid @RequestBody CreateStockMovementRequest request
    ) {
        return stockMovementService.createStockMovement(request);
    }

    @GetMapping
    public List<StockMovementResponse> getStockMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) StockMovementType movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return stockMovementService.getStockMovements(productId, movementType, from, to);
    }

    @GetMapping("/summary")
    public StockMovementSummaryResponse getStockMovementSummary(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return stockMovementService.getStockMovementSummary(productId, from, to);
    }
}