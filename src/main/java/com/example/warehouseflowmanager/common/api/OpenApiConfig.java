package com.example.warehouseflowmanager.common.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warehouseFlowManagerOpenApi() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Docker demo server")
                ))
                .info(new Info()
                        .title("Warehouse Flow Manager API")
                        .version("v1")
                        .description("""
                                REST API for managing warehouse products, storage locations, and stock movements.

                                API versioning:
                                - All application endpoints use the /api/v1 base path.

                                Main capabilities:
                                - Product CRUD with filtering, pagination, and sorting
                                - Product relocation between storage locations
                                - Storage location CRUD and stock overview
                                - Stock movements with INBOUND, OUTBOUND, and ADJUSTMENT flows
                                - Low-stock tracking
                                - Replenishment recommendations based on stock shortage and recent outbound demand
                                - Validation-aware JSON error responses
                                - Actuator health/metrics endpoints for local monitoring
                                """));
    }
}