package com.example.warehouseflowmanager.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(
        name = "ApiErrorResponse",
        description = "Standard JSON error response returned by the API for validation errors, missing resources, conflicts, and unexpected server errors"
)
public record ApiErrorResponse(

        @Schema(
                description = "Timestamp when the error occurred",
                example = "2026-04-27T17:30:00Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,

        @Schema(
                description = "HTTP status reason",
                example = "Bad Request"
        )
        String error,

        @Schema(
                description = "Human-readable error message",
                example = "Validation failed"
        )
        String message,

        @Schema(
                description = "Request path that caused the error",
                example = "/api/v1/products"
        )
        String path,

        @Schema(
                description = "Field-specific validation errors. This is null for non-validation errors.",
                example = "{\"sku\":\"SKU must not be blank\",\"quantity\":\"Quantity must be zero or greater\"}"
        )
        Map<String, String> validationErrors
) {
}