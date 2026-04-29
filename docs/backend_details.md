# Backend Details

This document explains the backend implementation of the Warehouse Flow Manager project.

The backend is a Java 21 / Spring Boot 4 application that exposes a versioned REST API for product management, storage location management, stock movement handling, low-stock monitoring, and replenishment support.

It uses PostgreSQL for persistence, Flyway for database migrations, Spring Data JPA for data access, Bean Validation for request validation, and SpringDoc OpenAPI for API documentation.

---

## Backend purpose

The backend models a small but realistic warehouse inventory workflow.

It is intentionally scoped as a portfolio project, but it includes backend concerns that appear in real business applications:

- layered architecture
- DTO-based API design
- request validation
- business rule enforcement
- stock movement workflows
- relational persistence
- database migrations
- pagination and sorting
- structured error responses
- integration testing
- OpenAPI documentation
- Docker-based runtime setup

---

## Architecture

The backend follows a layered Spring Boot architecture:

```text
External Clients
  ↓
REST Controllers
  ↓
Service Layer / Business Rules
  ↓
Spring Data JPA Repositories
  ↓
PostgreSQL Database
```

### Controller layer

Controllers expose HTTP endpoints and delegate business decisions to services.

Main controllers:

- `HealthController`
- `ProductController`
- `ProductStockMovementController`
- `StorageLocationController`
- `StockMovementController`

Controller responsibilities:

- define REST endpoints under `/api/v1`
- receive request bodies and query parameters
- trigger validation
- return response DTOs
- define HTTP response statuses
- provide OpenAPI endpoint descriptions

### Service layer

Services contain the main business logic.

Main services:

- `ProductService`
- `StorageLocationService`
- `StockMovementService`

Service responsibilities:

- enforce warehouse business rules
- validate stock movement requests
- calculate resulting stock quantities
- calculate low-stock and replenishment data
- coordinate repository calls
- map entities to response DTOs

### Repository layer

Repositories use Spring Data JPA for persistence.

Main repositories:

- `ProductRepository`
- `StorageLocationRepository`
- `StockMovementRepository`

Repository responsibilities:

- load and save entities
- check uniqueness
- support filtered product queries
- load stock movement history
- calculate stock movement summaries
- support pessimistic locking for stock updates

---

## Package structure

```text
src/main/java/com/example/warehouseflowmanager
├─ common
│  ├─ api
│  │  └─ ApiPaths
│  ├─ dto
│  │  └─ PagedResponse
│  └─ exception
│     ├─ ApiErrorResponse
│     ├─ BusinessRuleException
│     ├─ GlobalExceptionHandler
│     ├─ InvalidRequestException
│     ├─ ResourceConflictException
│     └─ ResourceNotFoundException
├─ controller
│  └─ HealthController
├─ config
│  └─ OpenApiConfig
├─ product
│  ├─ controller
│  ├─ dto
│  ├─ entity
│  ├─ repository
│  └─ service
├─ stockmovement
│  ├─ controller
│  ├─ dto
│  ├─ entity
│  ├─ repository
│  └─ service
├─ storagelocation
│  ├─ controller
│  ├─ dto
│  ├─ entity
│  ├─ repository
│  └─ service
└─ WarehouseFlowManagerApplication
```

The project is organized by feature while keeping shared API paths, DTO helpers, and exception handling under `common`.

---

## Domain model

The backend models three main warehouse concepts:

```text
StorageLocation
Product
StockMovement
```

A storage location can have many products.

A product belongs to one storage location and can have many stock movements.

A stock movement records a quantity-changing event for a product.

### Storage location

Represents a physical warehouse location.

Important fields:

- `id`
- `code`
- `zone`
- `description`
- `active`

### Product

Represents an inventory item.

Important fields:

- `id`
- `sku`
- `name`
- `description`
- `unit`
- `quantity`
- `minimumQuantity`
- `status`
- `storageLocation`

Possible product statuses:

```text
ACTIVE
BLOCKED
DISCONTINUED
```

### Stock movement

Represents a stock-changing event.

Important fields:

- `id`
- `product`
- `movementType`
- `quantity`
- `resultingQuantity`
- `note`
- `movementAt`

Possible movement types:

```text
INBOUND
OUTBOUND
ADJUSTMENT
```

---

## Business rules

### Product rules

- Product SKU must be unique.
- Products cannot be assigned to inactive storage locations.
- Blocked products cannot be created with initial stock.
- Blocked products cannot participate in stock movements.
- Product quantity cannot be changed through the update-product endpoint.
- Product quantity must be changed through stock movements only.
- Products cannot be deleted while stock exists.
- Products cannot be deleted when stock movement history exists.
- Relocation to the same storage location is rejected.

### Storage location rules

- Storage location code must be unique.
- Storage locations can be active or inactive.
- Products cannot be assigned to inactive locations.
- Storage locations cannot be deleted while products are assigned.

### Stock movement rules

- `INBOUND` movements increase stock.
- `OUTBOUND` movements decrease stock.
- `ADJUSTMENT` movements set stock to a counted quantity.
- Outbound quantity cannot exceed current stock.
- Stock movements are rejected for blocked products.
- Each stock movement stores the resulting quantity after the operation.

### Replenishment rules

Replenishment candidates are calculated from:

- current stock shortage against minimum quantity
- recent outbound demand
- recommended reorder quantity
- priority level

Priority values:

```text
CRITICAL
HIGH
MEDIUM
```

---

## Stock movement workflow

Stock changes are intentionally handled through stock movements instead of direct product updates.

```text
Create stock movement request
  ↓
Load product with lock
  ↓
Validate product status and quantity
  ↓
Calculate resulting quantity
  ↓
Update product quantity
  ↓
Insert stock movement history record
  ↓
Return stock movement response
```

This keeps stock changes traceable and prevents accidental quantity changes through normal product metadata updates.

---

## Database design

The PostgreSQL schema is managed through Flyway migrations.

Current migrations:

```text
src/main/resources/db/migration
├─ V1__init_schema.sql
├─ V2__align_current_schema.sql
├─ V3__drop_legacy_product_table.sql
└─ V4__make_storage_location_description_optional.sql
```

Main tables:

```text
storage_locations
products
stock_movements
```

Important database constraints include:

- unique product SKU
- unique storage location code
- foreign key from products to storage locations
- foreign key from stock movements to products
- product status check constraint
- stock movement type check constraint
- quantity validation constraints

Hibernate is configured with:

```text
spring.jpa.hibernate.ddl-auto=validate
```

This means:

- Flyway owns schema evolution
- Hibernate validates the schema on startup
- accidental schema drift is caught early

---

## Optional demo seed data

The project includes optional demo seed data:

```text
src/main/resources/db/demo/V100__seed_demo_data.sql
```

This file is intentionally stored under:

```text
db/demo
```

It is not stored under:

```text
db/migration
```

Therefore, Flyway does not run it automatically.

The seed file creates realistic demo data for:

- 8 products
- active storage locations
- one inactive storage location
- low-stock products
- one zero-stock critical product
- active, blocked, and discontinued product states
- inbound, outbound, and adjustment stock movements
- replenishment candidates

The script is loaded through:

```bash
./scripts/demo/seed-demo-data.sh
```

The seed script is re-runnable. It deletes and reinserts only the fixed demo records defined in the script, not arbitrary business data.

---

## API versioning and OpenAPI

All backend endpoints are versioned under:

```text
/api/v1
```

The path prefix is centralized in:

```text
ApiPaths
```

Swagger UI is available when the backend is running:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
http://localhost:8081/v3/api-docs
```

---

## Main API modules

### Health

```text
GET /api/v1/health
```

### Products

```text
GET    /api/v1/products
GET    /api/v1/products/{id}
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
PATCH  /api/v1/products/{id}/storage-location
GET    /api/v1/products/low-stock
GET    /api/v1/products/replenishment-candidates
```

### Product stock movements

```text
GET /api/v1/products/{id}/stock-movements
GET /api/v1/products/{id}/stock-movements/summary
```

### Storage locations

```text
GET    /api/v1/storage-locations
GET    /api/v1/storage-locations/{id}
POST   /api/v1/storage-locations
PUT    /api/v1/storage-locations/{id}
DELETE /api/v1/storage-locations/{id}
GET    /api/v1/storage-locations/{id}/stock-overview
```

### Stock movements

```text
GET  /api/v1/stock-movements
GET  /api/v1/stock-movements/summary
POST /api/v1/stock-movements
```

Manual backend testing with Swagger UI, Postman, and curl is documented in:

```text
docs/BACKEND_TESTING.md
```

---

## Validation and error handling

The backend uses Bean Validation and a central exception handler.

Shared error response type:

```text
ApiErrorResponse
```

Example validation error:

```json
{
  "timestamp": "2026-04-21T21:10:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "validationErrors": {
    "direction": "Direction must be either 'asc' or 'desc'"
  }
}
```

Example business rule error:

```json
{
  "timestamp": "2026-04-21T21:12:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Blocked products cannot be created with initial stock",
  "path": "/api/v1/products",
  "validationErrors": null
}
```

Handled error categories include:

| Situation                   | HTTP status                 |
|-----------------------------|-----------------------------|
| validation error            | `400 Bad Request`           |
| business rule failure       | `400 Bad Request`           |
| missing resource            | `404 Not Found`             |
| protected delete / conflict | `409 Conflict`              |
| unexpected server error     | `500 Internal Server Error` |

---

## Runtime setup

### Development runtime

Used for active development.

- PostgreSQL runs in Docker.
- Spring Boot usually runs locally.
- Backend usually runs on port `8080`.

Main files:

```text
docker-compose-dev.yml
.env.dev
scripts/dev/run-dev.sh
scripts/dev/stop-dev.sh
scripts/dev/reset-dev-db.sh
scripts/dev/run-dev-test.sh
```

### Demo runtime

- PostgreSQL runs in Docker.
- Spring Boot runs through Docker Compose.
- Backend runs on port `8081`.
- Optional demo seed data can be loaded.

Main files:

```text
docker-compose.yml
.env.example
.env
scripts/demo/run-demo.sh
scripts/demo/stop-demo.sh
scripts/demo/reset-demo-db.sh
scripts/demo/seed-demo-data.sh
scripts/demo/run-demo-test.sh
```

---

## Backend testing

The backend includes automated tests for:

- controller behavior
- validation
- business-rule enforcement
- stock movement logic
- product relocation rules
- storage location constraints
- error response format
- database-backed integration flows

Run backend tests through the demo setup:

```bash
./scripts/demo/run-demo-test.sh
```

Run backend tests through the development setup:

```bash
./scripts/dev/run-dev-test.sh
```

Manual backend testing is documented in:

```text
docs/BACKEND_TESTING.md
```

---

## Java monitoring note

The backend is a standard Spring Boot Java application, so it can be inspected with common JVM monitoring tools during local development.

Examples:

- Java VisualVM
- jProfiler
- Java Flight Recorder
- JVM heap and thread inspection tools

Useful things to inspect:

- heap usage
- thread count
- request behavior
- garbage collection behavior
- database-related runtime behavior

No special monitoring integration is required for this demo.

---

## What this backend demonstrates

This backend demonstrates:

- clean REST API design
- versioned API paths
- DTO-based request and response handling
- layered Spring Boot architecture
- business rule enforcement
- stock movement workflows
- relational data modeling
- PostgreSQL persistence
- Flyway database migrations
- pagination, filtering, and sorting
- central exception handling
- OpenAPI documentation
- automated backend testing
- Docker-based local/demo runtime setup