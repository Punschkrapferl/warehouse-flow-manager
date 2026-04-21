# Warehouse Flow Manager Backend

Spring Boot backend for managing warehouse products, storage locations, and stock movements.

The application exposes a REST API for core warehouse operations such as product management, stock updates, storage location assignment, low-stock monitoring, and replenishment support. It uses PostgreSQL for persistence, Flyway for schema versioning, and Swagger/OpenAPI for interactive API documentation.

---

## Overview

This project is a backend-focused warehouse operations demo built with **Spring Boot 4**, **Java 21**, **PostgreSQL**, and **Spring Data JPA**.

It is designed around a clear layered architecture:

- **Controllers** handle HTTP requests and validation
- **Services** implement business rules
- **Repositories** handle persistence
- **Flyway** manages database migrations
- **Swagger/OpenAPI** documents the API

The backend currently supports:

- Product CRUD
- Storage location CRUD
- Stock movement creation and querying
- Product relocation between storage locations
- Low-stock detection
- Replenishment recommendations based on shortage and recent outbound demand
- Validation-aware API error responses
- Integration tests against a real PostgreSQL-backed application context

---

## Main Features

### Product management
- Create, read, update, and delete products
- Filter by status and storage location
- Search by SKU or product name
- Paginate and sort product lists
- Prevent direct quantity changes through product updates
- Support relocation to another storage location through a dedicated endpoint

### Storage location management
- Create, read, update, and delete storage locations
- Prevent deletion when products are still assigned
- Mark locations as active or inactive
- Prevent assigning products to inactive locations
- Provide stock overview per storage location

### Stock movement handling
- Create `INBOUND`, `OUTBOUND`, and `ADJUSTMENT` movements
- Update product stock through business-safe rules
- Prevent outbound quantities from exceeding available stock
- Prevent stock movements for blocked products
- Keep stock movement history for traceability

### Inventory monitoring
- Return low-stock products
- Generate replenishment candidates
- Prioritize replenishment using:
  - current shortage against minimum quantity
  - recent outbound demand
  - business priority levels (`CRITICAL`, `HIGH`, `MEDIUM`)

### API quality
- Structured JSON error responses
- Bean validation for request bodies, path variables, and query parameters
- OpenAPI documentation via Swagger UI
- Health endpoint for smoke tests and local checks

---

## Architecture Diagram

## Architecture Diagram

```text
┌───────────────────────────────────────────────────────────────┐
│ Client / Frontend / Swagger UI                                │
│ Sends HTTP requests and receives JSON responses               │
└───────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────────┐
│ Spring Boot Application                                       │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ Controller Layer                                        │  │
│  │ - ProductController                                     │  │
│  │ - ProductStockMovementController                        │  │
│  │ - StorageLocationController                             │  │
│  │ - StockMovementController                               │  │
│  │ - HealthController                                      │  │
│  └─────────────────────────────────────────────────────────┘  │
│                              │                                │
│                              ▼                                │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ Service Layer                                           │  │
│  │ - ProductService                                        │  │
│  │ - StorageLocationService                                │  │
│  │ - StockMovementService                                  │  │
│  │                                                         │  │
│  │ Contains business rules such as:                        │  │
│  │ - stock validation                                      │  │
│  │ - relocation rules                                      │  │
│  │ - low-stock detection                                   │  │
│  │ - replenishment recommendation logic                    │  │
│  └─────────────────────────────────────────────────────────┘  │
│                              │                                │
│                              ▼                                │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ Repository Layer                                        │  │
│  │ - ProductRepository                                     │  │
│  │ - StorageLocationRepository                             │  │
│  │ - StockMovementRepository                               │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ Common / Cross-Cutting Components                       │  │
│  │ - GlobalExceptionHandler                                │  │
│  │ - ApiErrorResponse                                      │  │
│  │ - PagedResponse                                         │  │
│  │ - OpenApiConfig                                         │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────────┐
│ PostgreSQL Database                                           │
│ Tables:                                                       │
│ - storage_locations                                           │
│ - products                                                    │
│ - stock_movements                                             │
└───────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌───────────────────────────────────────────────────────────────┐
│ Flyway Migrations                                             │
│ - V1__init_schema.sql                                         │
│ - V2__align_current_schema.sql                                │
│ - V3__drop_legacy_product_table.sql                           │
└───────────────────────────────────────────────────────────────┘
```

---

## Architecture Notes

The backend follows a classic layered design.

### Controller layer

The controller layer exposes REST endpoints and validates incoming HTTP requests. It is responsible for:

* endpoint routing
* request/response mapping
* parameter validation
* HTTP status codes
* OpenAPI annotations

### Service layer

The service layer contains the main business logic, including:

* SKU uniqueness checks
* location activity checks
* low-stock detection
* replenishment calculation
* relocation rules
* stock update rules
* deletion constraints

### Repository layer

The repository layer uses Spring Data JPA to interact with PostgreSQL. It provides:

* CRUD access
* filtered queries
* stock movement aggregation queries
* pessimistic locking for concurrency-safe stock updates

### Common layer

The common layer centralizes reusable concerns such as:

* paginated response wrappers
* OpenAPI configuration
* custom exception types
* global error handling

---

## Domain Model

The system centers around three main entities:

### `StorageLocation`

Represents a physical warehouse location.

Fields:

* `id`
* `code`
* `zone`
* `description`
* `active`

### `Product`

Represents an inventory item.

Fields:

* `id`
* `sku`
* `name`
* `description`
* `unit`
* `quantity`
* `minimumQuantity`
* `status`
* `storageLocation`

### `StockMovement`

Represents a stock-changing event for a product.

Fields:

* `id`
* `product`
* `movementType`
* `quantity`
* `resultingQuantity`
* `note`
* `movementAt`

---

## Tech Stack

* **Java 21**
* **Spring Boot 4**
* **Spring Web**
* **Spring Validation**
* **Spring Data JPA**
* **Hibernate**
* **PostgreSQL**
* **Flyway**
* **Swagger / springdoc-openapi**
* **JUnit 5**
* **MockMvc**
* **Docker / Docker Compose**
* **Maven**

---

## Project Structure

```text
warehouse-flow-manager/
├── scripts/
│   ├── demo/
│   │   ├── reset-demo-db.sh
│   │   ├── run-demo.sh
│   │   ├── run-demo-test.sh
│   │   └── stop-demo.sh
│   └── dev/
│       ├── reset-dev-db.sh
│       ├── run-dev.sh
│       ├── run-dev-test.sh
│       └── stop-dev.sh
├── src/
│   ├── main/
│   │   ├── java/com/example/warehouseflowmanager/
│   │   │   ├── common/
│   │   │   │   ├── api/
│   │   │   │   ├── dto/
│   │   │   │   └── exception/
│   │   │   ├── controller/
│   │   │   ├── product/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── stockmovement/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── storagelocation/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── WarehouseFlowManagerApplication.java
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── application.yml
│   │       └── application-sql-debug.yml
│   └── test/
│       └── java/com/example/warehouseflowmanager/
│           ├── product/controller/
│           ├── stockmovement/controller/
│           ├── storagelocation/controller/
│           └── WarehouseFlowManagerApplicationTests.java
├── Dockerfile
├── docker-compose.yml
├── docker-compose-dev.yml
├── pom.xml
├── .env
├── .env.dev
└── .env.example
```

---

## API Modules

### Products

Base path: `/api/products`

Main endpoints:

* `POST /api/products`
* `GET /api/products`
* `GET /api/products/{id}`
* `PUT /api/products/{id}`
* `DELETE /api/products/{id}`
* `PATCH /api/products/{id}/storage-location`
* `GET /api/products/low-stock`
* `GET /api/products/replenishment-candidates`

### Product stock movements

Base path: `/api/products/{id}`

Main endpoints:

* `GET /api/products/{id}/stock-movements`
* `GET /api/products/{id}/stock-movements/summary`

### Storage locations

Base path: `/api/storage-locations`

Main endpoints:

* `POST /api/storage-locations`
* `GET /api/storage-locations`
* `GET /api/storage-locations/{id}`
* `PUT /api/storage-locations/{id}`
* `DELETE /api/storage-locations/{id}`
* `GET /api/storage-locations/{id}/stock-overview`

### Stock movements

Base path: `/api/stock-movements`

Main endpoints:

* `POST /api/stock-movements`
* `GET /api/stock-movements`
* `GET /api/stock-movements/summary`

### Health

* `GET /api/v1/health`

---

## Business Rules

The backend enforces several warehouse-specific rules:

* Product SKU must be unique
* Storage location code must be unique
* Products cannot be assigned to inactive storage locations
* Blocked products cannot be created with initial stock
* Blocked products cannot participate in stock movements
* Product quantity cannot be changed through the update-product endpoint
* Product quantity must be changed through stock movements only
* Outbound quantity cannot exceed current stock
* Storage locations cannot be deleted while products are assigned
* Products cannot be deleted while stock exists
* Products cannot be deleted when stock movement history exists
* Relocation to the same storage location is rejected

---

## Error Response Format

Validation and business errors are returned in a consistent JSON structure.

Example:

```json
{
  "timestamp": "2026-04-21T20:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "validationErrors": {
    "size": "Size must be greater than 0"
  }
}
```

---

## Running the Project

## Prerequisites

Make sure the following are installed:

* Java 21
* Maven Wrapper support (`./mvnw`)
* Docker
* Docker Compose

---

## Environment Files

### `.env`

```bash
cp .env.example .env
```
Used for the **demo/dockerized stack**.

### `.env.dev`

Used for **local development** where PostgreSQL runs in Docker and the Spring Boot app runs locally.

---

## Option 1: Local development mode

In this mode:

* PostgreSQL runs in Docker
* Spring Boot runs locally via Maven

Start development mode:

```bash
./scripts/dev/run-dev.sh
```

Stop development mode:

```bash
./scripts/dev/stop-dev.sh
```

Reset the dev database:

```bash
./scripts/dev/reset-dev-db.sh
```

Run tests in dev mode:

```bash
./scripts/dev/run-dev-test.sh
```

[Manual API Test Flow](TESTING.md)

---

## Option 2: Demo mode with Docker Compose

In this mode:

* PostgreSQL runs in Docker
* the application also runs in Docker
* the full stack is started through `docker-compose.yml`

Start demo mode:

```bash
./scripts/demo/run-demo.sh
```

Stop demo mode:

```bash
./scripts/demo/stop-demo.sh
```

Reset the demo database:

```bash
./scripts/demo/reset-demo-db.sh
```

Run tests against the demo database setup:

```bash
./scripts/demo/run-demo-test.sh
```

[Manual API Test Flow](TESTING.md)

---

## API Documentation

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

or, if running the demo configuration shown above:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

## Database Migrations

The project uses **Flyway** for schema migration.

Current migrations:

* `V1__init_schema.sql`
* `V2__align_current_schema.sql`
* `V3__drop_legacy_product_table.sql`

Hibernate is configured with:

```yaml
spring.jpa.hibernate.ddl-auto=validate
```

This means:

* Flyway owns schema evolution
* Hibernate validates the schema on startup
* accidental schema drift is caught early

---

## Testing

The project includes backend integration tests for the main API modules.

Covered areas include:

* product creation
* initial stock movement creation
* relocation rules
* inactive storage location handling
* replenishment candidate calculation
* malformed JSON handling
* invalid query parameter handling
* stock movement validation
* storage location deletion rules

Test stack:

* `SpringBootTest`
* `MockMvc`
* `JdbcTemplate`
* real database-backed integration flows

### Important note about test scope

The current automated tests are primarily **integration tests**, not browser-based end-to-end tests.

That means they do test:

* controller layer
* validation
* service logic
* repository/database interaction

But they do not test:

* a real frontend
* browser user flows
* a fully deployed external environment

---

## Example Highlights

A few noteworthy implementation details:

* stock updates use **pessimistic locking** to avoid concurrent inventory inconsistencies
* product listing supports filtering, pagination, and safe sorting
* replenishment recommendations combine **minimum stock shortage** with **recent outbound demand**
* low-stock logic is consistent across product and storage-location views
* OpenAPI descriptions are written directly in controller annotations for easy exploration in Swagger UI

---

## Current Status

The backend demonstrates:

* clean modular structure
* real persistence
* migration-based schema management
* documented REST endpoints
* business rule enforcement
* integration testing
* Docker-based local execution

---

## Possible Next Steps

Good future improvements for a more advanced version:

* authentication and role-based authorization
* reservation / picking workflows
* supplier and purchase order management
* audit logging
* reporting dashboards
* CI pipeline for automated test runs
* Testcontainers-based test setup
* fuller end-to-end testing with a frontend client

---

## License

MIT License

Copyright (c) 2026 Punschkrapferl

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


