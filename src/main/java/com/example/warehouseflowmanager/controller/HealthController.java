package com.example.warehouseflowmanager.controller;

import com.example.warehouseflowmanager.common.api.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
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
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "warehouse-flow-manager",
                "timestamp", Instant.now().toString()
        );
    }
}