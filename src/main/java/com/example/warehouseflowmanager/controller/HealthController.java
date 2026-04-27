package com.example.warehouseflowmanager.controller;

import com.example.warehouseflowmanager.common.api.ApiPaths;
import com.example.warehouseflowmanager.common.api.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
        name = "Health",
        description = "Simple application health endpoint for smoke tests and demo readiness checks"
)
public class HealthController {

    @GetMapping(ApiPaths.HEALTH)
    @Operation(
            summary = "Get application health",
            description = "Returns a small runtime status payload for local smoke tests, Docker health checks, and recruiter demos"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Application is running",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HealthResponse.class)
            )
    )
    public HealthResponse health() {
        return new HealthResponse(
                "UP",
                "warehouse-flow-manager",
                Instant.now().toString()
        );
    }
}