package com.example.warehouseflowmanager.stockmovement.controller;

import com.example.warehouseflowmanager.stockmovement.dto.CreateStockMovementRequest;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) Long productId
    ) {
        return stockMovementService.getStockMovements(productId);
    }
}