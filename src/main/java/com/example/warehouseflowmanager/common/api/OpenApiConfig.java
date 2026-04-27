package com.example.warehouseflowmanager.common.api;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
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
                .tags(List.of(
                        new Tag()
                                .name("Health")
                                .description("Application health and demo readiness checks"),
                        new Tag()
                                .name("Products")
                                .description("Product management, filtering, pagination, low-stock tracking, replenishment, and relocation"),
                        new Tag()
                                .name("Product Stock Movements")
                                .description("Product-specific stock movement history and summaries"),
                        new Tag()
                                .name("Stock Movements")
                                .description("Inbound, outbound, and adjustment stock movement workflows"),
                        new Tag()
                                .name("Storage Locations")
                                .description("Storage location management and location-based stock overview")
                ))
                .info(new Info()
                        .title("Warehouse Flow Manager API")
                        .version("v1")
                        .description("""
                                REST API for managing warehouse products, storage locations, stock movements, low-stock tracking, and replenishment workflows.

                                This project is built as a full-stack Java/Angular job-application project.
                                It demonstrates a maintainable Spring Boot backend with PostgreSQL, Flyway migrations,
                                validation, global error handling, integration tests, OpenAPI documentation, Docker setup,
                                and an Angular frontend.

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
                                - Health endpoint for local smoke tests and demo readiness checks
                                """)
                        .contact(new Contact()
                                .name("Warehouse Flow Manager")
                                .url("https://github.com/Punschkrapferl/warehouse-flow-manager"))
                        .license(new License()
                                .name("Portfolio / Demo Project")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project repository")
                        .url("https://github.com/Punschkrapferl/warehouse-flow-manager"));
    }
}