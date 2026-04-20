package com.example.warehouseflowmanager.common.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warehouseFlowManagerOpenApi() {
        // Central OpenAPI metadata used by Swagger UI and generated API docs.
        // Keep this description short and recruiter-friendly so the project is easy
        // to understand during quick review.
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Flow Manager API")
                        .version("v1")
                        .description("""
                                REST API for managing warehouse products, storage locations, and stock movements.

                                Main capabilities:
                                - Product CRUD with filtering, pagination, and sorting
                                - Storage location CRUD and stock overview
                                - Stock movements with INBOUND, OUTBOUND, and ADJUSTMENT flows
                                - Low-stock tracking
                                - Validation-aware error responses
                                """));
    }
}