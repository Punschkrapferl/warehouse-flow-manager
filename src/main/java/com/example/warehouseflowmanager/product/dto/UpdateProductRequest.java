package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

        @NotBlank(message = "SKU is required")
        private String sku;

        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        @NotBlank(message = "Unit is required")
        private String unit;

        @Null(message = "Quantity cannot be changed through product update. Use stock movements instead")
        private Integer quantity;

        @PositiveOrZero(message = "Minimum quantity must be zero or greater")
        private Integer minimumQuantity;

        private ProductStatus status;

        private Long storageLocationId;
}