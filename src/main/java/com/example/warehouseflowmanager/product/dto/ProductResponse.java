package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private String unit;
    private Integer quantity;
    private Long storageLocationId;
    private String storageLocationCode;
    private ProductStatus status;
}