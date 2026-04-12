package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for creating a product")
public class CreateProductRequest {

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        @Schema(
                description = "Unique stock keeping unit of the product",
                example = "SKU-5001"
        )
        private String sku;

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        @Schema(
                description = "Human-readable product name",
                example = "Heavy Duty Container"
        )
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(
                description = "Optional product description",
                example = "Container for warehouse transport"
        )
        private String description;

        @NotBlank(message = "Unit is required")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        @Schema(
                description = "Measurement unit of the product",
                example = "piece"
        )
        private String unit;

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be zero or greater")
        @Schema(
                description = "Initial stock quantity. If greater than zero, an initial INBOUND stock movement is created automatically",
                example = "10",
                minimum = "0"
        )
        private Integer quantity;

        @PositiveOrZero(message = "Minimum quantity must be zero or greater")
        @Schema(
                description = "Low-stock threshold",
                example = "2",
                minimum = "0",
                nullable = true
        )
        private Integer minimumQuantity;

        @Schema(
                description = "Product status",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "DISCONTINUED"},
                nullable = true
        )
        private ProductStatus status;

        @Positive(message = "Storage location ID must be greater than 0")
        @Schema(
                description = "Optional storage location ID assigned to the product",
                example = "1",
                minimum = "1",
                nullable = true
        )
        private Long storageLocationId;
}