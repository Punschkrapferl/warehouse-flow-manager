package com.example.warehouseflowmanager.storagelocation.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StorageLocationStockItemResponse {

    private final Long id;
    private final String sku;
    private final String name;
    private final String unit;
    private final Integer quantity;
    private final Integer minimumQuantity;
    private final ProductStatus status;
    private final boolean lowStock;
}