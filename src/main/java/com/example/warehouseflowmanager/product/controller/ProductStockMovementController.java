package com.example.warehouseflowmanager.product.controller;

import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementSummaryResponse;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductStockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping("/{id}/stock-movements")
    public List<StockMovementResponse> getProductStockMovements(
            @PathVariable Long id,
            @RequestParam(required = false) StockMovementType movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return stockMovementService.getStockMovementsByProductId(id, movementType, from, to);
    }

    @GetMapping("/{id}/stock-movements/summary")
    public StockMovementSummaryResponse getProductStockMovementSummary(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return stockMovementService.getStockMovementSummaryByProductId(id, from, to);
    }
}