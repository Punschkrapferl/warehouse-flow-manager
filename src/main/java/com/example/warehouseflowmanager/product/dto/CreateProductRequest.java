package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

        @NotBlank(message = "SKU must not be blank")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        private String sku;

        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        private String name;

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;

        @NotBlank(message = "Unit must not be blank")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        private String unit;

        @Min(value = 0, message = "Quantity must be greater than or equal to 0")
        private Integer quantity;

        @NotBlank(message = "Location code must not be blank")
        @Size(max = 50, message = "Location code must not exceed 50 characters")
        @Pattern(
                regexp = "^[A-Z0-9-]+$",
                message = "Location code must contain only uppercase letters, numbers, and hyphens"
        )
        private String locationCode;

        private ProductStatus status;
}