# Warehouse Flow Manager

Backend-focused warehouse operations demo built with **Java 21**, **Spring Boot**, and **PostgreSQL**.

The project models a small but realistic warehouse domain with **products**, **storage locations**, and **stock movements**. It is designed to showcase the backend engineering concerns that matter most in portfolio reviews and interviews: **clean API design, validation, business rules, persistence, transaction safety, testing, documentation, and Docker-based delivery**.

The repository can be reviewed in two practical ways:

- **from source** via `docker compose up --build`
- **from the released Docker image** via `docker pull punschkrapferl23/warehouse-flow-manager:1.0.0`

---

## Quick start (TL;DR for reviewers)

### 1. Clone the repository

```bash
git clone https://github.com/Punschkrapferl/warehouse-flow-manager
cd warehouse-flow-manager
```

### 2. Choose one run mode
#### Option A: Run the full stack from the repository
```bash
docker compose up --build
```

This starts:
- PostgreSQL
- the Spring Boot application

#### Option B: Run the released Docker image
Pull the published application image:
```bash
docker pull punschkrapferl23/warehouse-flow-manager:1.0.0
```

Then run PostgreSQL:
```bash
docker run --name warehouse-flow-postgres \
  -e POSTGRES_DB=warehouse_flow_manager \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:17
```

Then run the application image:
```bash
docker run --name warehouse-flow-manager \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=warehouse_flow_manager \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  punschkrapferl23/warehouse-flow-manager:1.0.0
```

### 3. Open the project
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health endpoint: http://localhost:8080/api/v1/health

### 4. Verify the service
```bash
curl http://localhost:8080/api/v1/health
```

Expected response:
```JSON
{
  "status": "UP",
  "service": "warehouse-flow-manager",
  "timestamp": "2026-04-12T21:15:00Z"
}
```

### 5. Stop the stack
If you used Docker Compose:
```bash
docker compose down
```

If you started containers manually:
```bash
docker stop warehouse-flow-manager warehouse-flow-postgres
docker rm warehouse-flow-manager warehouse-flow-postgres
```

--- 

## What this demonstrates
- Spring Boot REST API design with a clear feature-based package structure
- CRUD operations for warehouse products and storage locations
- Inventory updates through explicit stock movements instead of unsafe direct quantity edits
- Realistic warehouse business rules instead of purely mechanical CRUD
- Validation, exception handling, and consistent API responses
- PostgreSQL's persistence with Flyway-managed schema migrations
- OpenAPI / Swagger integration for fast API exploration
- Dockerized local setup with PostgreSQL and the application
- Integration tests for core backend flows
  
The repository includes a released Docker image for quick review:
- `punschkrapferl23/warehouse-flow-manager:1.0.0`

The application was also kept easy to run directly from source with:
- `docker compose up --build`

--- 

## Project purpose
The application manages a simple warehouse domain through three core areas:

### Products:
Warehouse items with SKU, quantity, status, minimum stock threshold, and assigned storage location

### Storage Locations
Physical warehouse locations identified by code and zone

### Stock Movements
Inventory changes recorded explicitly as:
- `INBOUND`
- `OUTBOUND`
- `ADJUSTMENT`

Instead of allowing arbitrary quantity changes, stock updates are intentionally routed through stock movement operations so that inventory changes remain controlled, traceable, and business-rule aware.

---

## Architecture
### High-level components:

#### Product module:
- Product CRUD
- Filtering / pagination / sorting
- Low-stock handling
- Product-specific movement history

#### Storage location module:
- Storage location CRUD
- Stock overview per location

#### Stock movement module:
- Inbound, outbound, and adjustment operations
- Concurrency-aware inventory updates
- Movement filtering and summary endpoints

#### Shared common modules:
- API responses
- Shared DTO infrastructure
- Centralized exception handling

The project follows a feature-oriented package structure.

#### Main feature packages:
- `product`
- `storagelocation`
- `stockmovement`

#### Each feature keeps the same internal layout:
- `controller`
- `dto`
- `entity`
- `repository`
- `service`

#### Shared components live in:
- `common.api`
- `common.dto`
- `common.exception`
This keeps the codebase easy to navigate during review and aligns well with a clean recruiter-facing backend structure.

---

## Tech stack
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Jakarta Validation
- SpringDoc OpenAPI / Swagger
- JUnit / Spring Boot integration tests
- Docker
- Docker Compose

---

## Main features
### Product management
- create products
- list products
- fetch product by ID
- update product metadata
- delete products with business-rule safeguards
- filter, paginate, and sort product lists
- track low-stock products

### Storage location management
- create storage locations
- list storage locations
- fetch storage location by ID
- update storage locations
- delete storage locations when allowed
- inspect stock overview per location

### Stock movement management
- create `INBOUND`, `OUTBOUND`, and `ADJUSTMENT` movements
- update product inventory through stock operations
- retrieve movement history for a product
- filter stock movements
- query stock movement summaries

---

## Key business rules
These rules are implemented in the current codebase and are a central part of the project:
- product quantity cannot be changed directly through product update requests
- initial stock on product creation automatically creates a stock movement record
- blocked products cannot be created with initial stock
- stock movement creation uses database locking to reduce concurrency issues during inventory updates
- product deletion is restricted when stock or movement-history constraints apply
- storage locations cannot be deleted when products are still assigned
- low-stock logic only counts products with status `ACTIVE`
- product status values are: `ACTIVE`, `BLOCKED`, `DISCONTINUED`

---

## API overview
Swagger / OpenAPI is already integrated.

### URLs
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
The API centers around these resource groups:
    
### Products
- product CRUD
- product filtering / pagination / sorting
- product stock movement history
    
### Storage Locations
- storage location CRUD
- stock overview per storage location
    
### Stock Movements
- create stock movements
- filter stock movements
- stock movement summary endpoints
    
### Health
- service health check endpoint for runtime verification
A production-oriented profile is configured to disable SpringDoc when needed.

---

## Health endpoint
Request:
```http request
GET /api/v1/health
```

Response:
```JSON
{
  "status": "UP",
  "service": "warehouse-flow-manager",
  "timestamp": "2026-04-12T21:15:00Z"
}
```
This endpoint is useful for smoke tests, Docker verification, and quick runtime checks.

---

## Prerequisites
To run with Docker:
- Docker Desktop or Docker Engine
- Docker Compose v2 (docker compose)

To run locally without Docker for the application process:
- Java 21
- Maven Wrapper (./mvnw is included)
- PostgreSQL running separately, or started with:
```bash
docker compose up -d postgres
```

---

## Run modes
### 1. Run from source with Docker Compose
This is the easiest way to review the repository from source.
From the project root:
```bash
docker compose up --build
```
Docker will:
- start PostgreSQL
- build the Spring Boot application image from the repository
- start the application container
- wait for PostgreSQL health before starting the app container

Open:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health endpoint: `http://localhost:8080/api/v1/health`

To stop the stack:
```bash
docker compose down
```

To also remove volumes:
```bash
docker compose down -v
```

### 2. Run locally with Maven
Use this mode during normal development when you want to run the application directly.

Start PostgreSQL:
```bash
docker compose up -d postgres
```

Run the application:
```bash
./mvnw spring-boot:run
```

On Windows:
```bat
mvnw.cmd spring-boot:run
```

Verify:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health endpoint: `http://localhost:8080/api/v1/health`

### 3. Run the released Docker image
Use this mode when you want to review the published application image directly.

Pull the application image:
```bash
docker pull punschkrapferl23/warehouse-flow-manager:1.0.0
```

Start PostgreSQL:
```bash
docker run --name warehouse-flow-postgres \
  -e POSTGRES_DB=warehouse_flow_manager \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:17
 ```

Start the application:
```bash
docker run --name warehouse-flow-manager \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=warehouse_flow_manager \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  punschkrapferl23/warehouse-flow-manager:1.0.0
 ```

Then open:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health endpoint: `http://localhost:8080/api/v1/health`

---

## Example workflow

1. Clone repository
```bash
git clone https://github.com/Punschkrapferl/warehouse-flow-manager
cd warehouse-flow-manager
```

2. Start PostgreSQL only
```bash
docker compose up -d postgres 
```

3. Start the Spring Boot app
```bash
./mvnw spring-boot:run
```

4. Open Swagger
`http://localhost:8080/swagger-ui/index.html`

5. Check health
```bash
curl http://localhost:8080/api/v1/health
```

6. Run tests
```bash
./mvnw test
```

---

## Tests
The project already includes integration-oriented test coverage for the main API areas.

Current test classes:
- `WarehouseFlowManagerApplicationTests`
- `ProductControllerIntegrationTest`
- `StorageLocationControllerIntegrationTest`
- `StockMovementControllerIntegrationTest`

Run all tests with:
```bash
./mvnw test
```

On Windows:
```bat
mvnw.cmd test
```
These tests verify that application wiring, API flows, persistence, and business rules work together correctly.

---

## Configuration notes
The configuration is designed to stay practical for both local development and containerized runs.

Current setup:
- `application.yml` uses environment variables with localhost defaults
- a dedicated `sql-debug` profile is available
- the `prod` profile disables SpringDoc
- Docker support is included through:
  - `Dockerfile`
  - `docker-compose.yml`
  - `.dockerignore`

Docker image release:
Published image:
- `punschkrapferl23/warehouse-flow-manager:1.0.0`
- `punschkrapferl23/warehouse-flow-manager:latest`

Recommended stable pull:
```bash
docker pull punschkrapferl23/warehouse-flow-manager:1.0.0
```

---

## Project Structure

```
warehouse-flow-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/warehouseflowmanager/
│   │   │       ├── common/
│   │   │       │   ├── api/
│   │   │       │   ├── dto/
│   │   │       │   └── exception/
│   │   │       ├── product/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── entity/
│   │   │       │   ├── repository/
│   │   │       │   └── service/
│   │   │       ├── storagelocation/
│   │   │       │   ├── controller/
│   │   │       │   ├── dto/
│   │   │       │   ├── entity/
│   │   │       │   ├── repository/
│   │   │       │   └── service/
│   │   │       └── stockmovement/
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           ├── entity/
│   │   │           ├── repository/
│   │   │           └── service/
│   │   └── resources/
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── pom.xml
└── README.md
```

---

## Troubleshooting
### 1. Swagger UI is not available
Check whether you are running with the production profile.
The prod profile disables SpringDoc.
For local development, use the normal run mode instead of prod.

### 2. The app cannot connect to PostgreSQL
Make sure PostgreSQL is running.

For local development:
```bash
docker compose up -d postgres
```
Then start the app again.

Also confirm that your database environment variables match the running database.

### 3. Docker Compose starts but the API is not reachable

Restart cleanly:
```bash
docker compose down
docker compose up --build
```

Then test:
```bash
curl http://localhost:8080/api/v1/health
```

### 4. Port 8080 is already in use
Stop the conflicting process or change the mapped port in your Docker or local run setup.

### 5. Tests fail because the database state is inconsistent

Restart from a clean Docker state if needed:
```bash 
docker compose down -v
docker compose up --build
```

Then rerun:
```bash
./mvnw test
```

---

## MIT License
Copyright (c) 2026 Punschkrapferl

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

