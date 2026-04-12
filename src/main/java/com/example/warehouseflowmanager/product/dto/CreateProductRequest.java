package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        private String sku;

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @NotBlank(message = "Unit is required")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        private String unit;

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be zero or greater")
        private Integer quantity;

        @PositiveOrZero(message = "Minimum quantity must be zero or greater")
        private Integer minimumQuantity;

        private ProductStatus status;

        @Positive(message = "Storage location ID must be greater than 0")
        private Long storageLocationId;
}