package com.example.warehouseflowmanager.product.dto;

import com.example.warehouseflowmanager.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
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
@Schema(description = "Request payload for updating product metadata")
public class UpdateProductRequest {

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        @Schema(
                description = "Unique stock keeping unit of the product",
                example = "SKU-5001",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String sku;

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        @Schema(
                description = "Human-readable product name",
                example = "Heavy Duty Container",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(
                description = "Optional product description",
                example = "Container for warehouse transport",
                maxLength = 500,
                nullable = true
        )
        private String description;

        @NotBlank(message = "Unit is required")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        @Schema(
                description = "Measurement unit of the product",
                example = "piece",
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String unit;

        @Null(message = "Quantity cannot be changed through product update. Use stock movements instead")
        @Schema(
                description = "Must not be provided. Product quantity must be changed through stock movements instead",
                nullable = true
        )
        private Integer quantity;

        @PositiveOrZero(message = "Minimum quantity must be zero or greater")
        @Schema(
                description = "Low-stock threshold. Defaults to 0 when omitted",
                example = "2",
                minimum = "0",
                nullable = true
        )
        private Integer minimumQuantity;

        @Schema(
                description = "Product status. Existing status is kept when omitted",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "DISCONTINUED"},
                nullable = true
        )
        private ProductStatus status;

        @Positive(message = "Storage location ID must be greater than 0")
        @Schema(
                description = "Optional storage location ID assigned to the product. Null removes the storage assignment",
                example = "1",
                minimum = "1",
                nullable = true
        )
        private Long storageLocationId;
}