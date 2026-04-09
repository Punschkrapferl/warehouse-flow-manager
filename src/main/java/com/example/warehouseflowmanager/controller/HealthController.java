package com.example.warehouseflowmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * First controller in the project.
 *
 * Purpose:
 * Provide a very small test endpoint to verify that the backend starts
 * correctly and is reachable through the browser or an HTTP client.
 *
 * This is only a bootstrap endpoint.
 * It does not contain real warehouse business logic yet.
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public String health() {
        return "Warehouse Flow Manager backend is running";
    }
}
