package com.example.warehouseflowmanager.storagelocation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StorageLocationStockOverviewResponse {

    private final Long id;
    private final String code;
    private final String zone;
    private final String description;
    private final Boolean active;
    private final int totalProducts;
    private final int totalQuantity;
    private final int lowStockProductCount;
    private final List<StorageLocationStockItemResponse> products;
}