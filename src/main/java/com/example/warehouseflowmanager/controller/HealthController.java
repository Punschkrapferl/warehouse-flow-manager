package com.example.warehouseflowmanager.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        // Very small runtime verification endpoint.
        // Useful for local smoke tests, Docker health checks, and recruiter demos.
        return Map.of(
                "status", "UP",
                "service", "warehouse-flow-manager",
                "timestamp", Instant.now().toString()
        );
    }
}