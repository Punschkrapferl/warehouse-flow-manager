package com.example.warehouseflowmanager.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "HealthResponse",
        description = "Simple runtime health response used for smoke tests and demo readiness checks"
)
public record HealthResponse(

        @Schema(
                description = "Application runtime status",
                example = "UP"
        )
        String status,

        @Schema(
                description = "Service name",
                example = "warehouse-flow-manager"
        )
        String service,

        @Schema(
                description = "Timestamp when the health response was created",
                example = "2026-04-27T17:30:00Z"
        )
        String timestamp
) {
}