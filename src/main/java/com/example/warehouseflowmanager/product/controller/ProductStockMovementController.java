package com.example.warehouseflowmanager.product.controller;

import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductStockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping("/{id}/stock-movements")
    public List<StockMovementResponse> getProductStockMovements(@PathVariable Long id) {
        return stockMovementService.getStockMovementsByProductId(id);
    }
}