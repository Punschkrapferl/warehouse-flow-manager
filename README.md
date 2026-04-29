# Warehouse Flow Manager

Full-stack warehouse operations demo built with **Java 21**, **Spring Boot 4**, **PostgreSQL**, **Flyway**, **Angular**, **TypeScript**, **Docker**, **OpenAPI**, and **Playwright**.

The application provides a versioned REST API and an Angular operations-console frontend for managing warehouse products, storage locations, stock movements, low-stock monitoring, and replenishment support.

---

## Overview

Warehouse Flow Manager demonstrates:

- versioned REST API design under `/api/v1`
- Spring Boot layered backend architecture
- PostgreSQL persistence with Flyway migrations
- warehouse-specific business rules
- stock movement workflows
- low-stock and replenishment logic
- Angular operations-console frontend
- centralized frontend API services
- feature facades for frontend state handling
- frontend error mapping
- backend integration tests
- frontend unit/API tests
- mocked Playwright E2E tests
- Docker-based development and demo workflows
- optional demo seed data for portfolio screenshots

---

## Tech stack

### Backend

- Java 21
- Spring Boot 4
- Spring Web
- Spring Validation
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- SpringDoc OpenAPI / Swagger UI
- JUnit 5
- MockMvc
- Maven

### Frontend

- Angular
- TypeScript
- SCSS
- Angular HTTP client
- feature facade pattern
- centralized API services
- Playwright
- nginx for Dockerized frontend runtime

### Infrastructure

- Docker
- Docker Compose
- Maven Wrapper
- npm
- shell scripts for development and demo workflows

---

## Screenshots

### Operations Dashboard

The dashboard gives a quick overview of API health, low-stock risk, critical shortages, replenishment urgency, and recent warehouse activity.

![Operations Dashboard](docs/screenshots/dashboard-overview.png)

### Product Inventory

The product inventory view shows paginated stock data, product status, current quantity, minimum quantity, stock gaps, assigned locations, and low-stock indicators.

![Product Inventory](docs/screenshots/product-inventory.png)

### Storage Location Visibility

The storage location view shows active and inactive warehouse locations, assigned stock, location-level quantities, and low-stock exposure per location.

![Storage Location Visibility](docs/screenshots/storage-locations.png)

### Stock Movement Monitoring

The stock movement view shows inbound, outbound, and adjustment activity with traceable stock history.

![Stock Movement Monitoring](docs/screenshots/stock-movements.png)

### Swagger / OpenAPI Documentation

The backend exposes documented, versioned REST endpoints under `/api/v1` through Swagger UI.

![Swagger OpenAPI Documentation](docs/screenshots/swagger-openapi.png)

---

## Main features

### Product management

- create, read, update, and delete products
- filter by status and storage location
- search by SKU or product name
- paginate and sort product lists
- prevent direct quantity changes through product updates
- relocate products through a dedicated endpoint

### Storage location management

- create, read, update, and delete storage locations
- prevent deletion while products are assigned
- mark locations as active or inactive
- prevent assigning products to inactive locations
- show stock overview per storage location

### Stock movement handling

- create `INBOUND`, `OUTBOUND`, and `ADJUSTMENT` movements
- update product stock through controlled business rules
- prevent outbound quantities from exceeding available stock
- prevent stock movements for blocked products
- keep stock movement history for traceability

### Inventory monitoring

- show low-stock products
- generate replenishment candidates
- prioritize replenishment using:
  - current shortage
  - recent outbound demand
  - priority levels: `CRITICAL`, `HIGH`, `MEDIUM`

---

## Architecture

```text
Angular Frontend
  ↓ HTTP / JSON
Spring Boot REST API
  ↓ Spring Data JPA
PostgreSQL Database
```

The backend follows a layered architecture:

```text
REST Controllers
  ↓
Service Layer / Business Rules
  ↓
Spring Data JPA Repositories
  ↓
PostgreSQL
```

Flyway manages schema migrations separately from application logic.

More details:

- [Backend details](docs/backend_details.md)
- [Frontend details](docs/frontend_details.md)
- [Backend testing guide](docs/BACKEND_TESTING.md)
- [Frontend testing guide](docs/FRONTEND_TESTING.md)

---

## Main business rules

- Product SKU must be unique.
- Storage location code must be unique.
- Products cannot be assigned to inactive storage locations.
- Blocked products cannot be created with initial stock.
- Blocked products cannot participate in stock movements.
- Product quantity cannot be changed through the update-product endpoint.
- Product quantity must be changed through stock movements only.
- Outbound quantity cannot exceed current stock.
- Storage locations cannot be deleted while products are assigned.
- Products cannot be deleted while stock exists.
- Products cannot be deleted when stock movement history exists.
- Relocation to the same storage location is rejected.

---

## Project structure

```text
warehouse-flow-manager
├─ docs
│  ├─ screenshots
│  │  ├─ dashboard-overview.png
│  │  ├─ product-inventory.png
│  │  ├─ stock-movements.png
│  │  ├─ storage-locations.png
│  │  └─ swagger-openapi.png
│  ├─ backend_details.md
│  ├─ frontend_details.md
│  ├─ BACKEND_TESTING.md
│  └─ FRONTEND_TESTING.md
├─ frontend
│  ├─ e2e
│  ├─ src
│  ├─ Dockerfile
│  ├─ docker-compose.yml
│  ├─ nginx.conf
│  ├─ proxy.conf.json
│  ├─ proxy.demo.conf.json
│  ├─ package.json
│  ├─ angular.json
│  └─ playwright.config.ts
├─ scripts
│  ├─ demo
│  └─ dev
├─ src
│  ├─ main
│  │  ├─ java/com/example/warehouseflowmanager
│  │  └─ resources
│  │     ├─ db/migration
│  │     ├─ db/demo
│  │     ├─ application.yml
│  │     ├─ application-demo.yml
│  │     └─ application-sql-debug.yml
│  └─ test
│     └─ java/com/example/warehouseflowmanager
├─ docker-compose.yml
├─ docker-compose-dev.yml
├─ Dockerfile
├─ pom.xml
└─ README.md
```

---

## Runtime modes

The project separates development and demo runtime paths.

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

Used for reviewer walkthroughs and portfolio presentation.

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

## Quick start

### Demo backend

Prerequisites:

- Docker Desktop installed and running
- port `8081` free

Start demo backend:

```bash
git clone https://github.com/Punschkrapferl/warehouse-flow-manager
cd warehouse-flow-manager

cp .env.example .env
./scripts/demo/run-demo.sh
```

Open Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

Open OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

Stop demo backend:

```bash
./scripts/demo/stop-demo.sh
```

Reset demo database:

```bash
./scripts/demo/reset-demo-db.sh
./scripts/demo/run-demo.sh
```

Run backend tests against the demo setup:

```bash
./scripts/demo/run-demo-test.sh
```

---

## Optional demo seed data

The project includes optional demo seed data:

```text
src/main/resources/db/demo/V100__seed_demo_data.sql
```

This file is intentionally stored under `db/demo`, not `db/migration`.

That means Flyway does **not** run it automatically.

The seed file creates realistic demo data for:

- products
- storage locations
- stock movements
- low-stock products
- replenishment candidates
- active, blocked, and discontinued stock states

Load the optional seed data after starting the demo backend:

```bash
./scripts/demo/seed-demo-data.sh
```

The seed script is re-runnable. It deletes and reinserts only the fixed demo records defined in the script, not arbitrary business data.

---

## Frontend quick start

### Local Angular runtime

For local development with the backend running on `localhost:8080`:

```bash
cd frontend
npm install
npm start
```

The Angular app runs on:

```text
http://localhost:4200
```

The local proxy file is:

```text
frontend/proxy.conf.json
```

It forwards `/api` requests to:

```text
http://localhost:8080
```

### Demo Angular runtime

For demo mode with the Docker backend running on `localhost:8081`:

```bash
cd frontend
npm run start:demo
```

The demo proxy file is:

```text
frontend/proxy.demo.conf.json
```

It forwards `/api` requests to:

```text
http://localhost:8081
```

### Dockerized frontend runtime

The frontend can also run as a Dockerized nginx build.

The Dockerized frontend serves the Angular production build and proxies `/api/` calls to the demo backend.

Start the Dockerized frontend:

```bash
cd frontend
docker compose up -d --build
```

Open:

```text
http://localhost:4200
```

Stop the Dockerized frontend:

```bash
cd frontend
docker compose down
```

---

## API overview

All backend endpoints are versioned under:

```text
/api/v1
```

Main endpoint groups:

- `/api/v1/health`
- `/api/v1/products`
- `/api/v1/products/{id}/stock-movements`
- `/api/v1/products/{id}/stock-movements/summary`
- `/api/v1/products/{id}/storage-location`
- `/api/v1/products/low-stock`
- `/api/v1/products/replenishment-candidates`
- `/api/v1/storage-locations`
- `/api/v1/storage-locations/{id}/stock-overview`
- `/api/v1/stock-movements`
- `/api/v1/stock-movements/summary`

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8081/swagger-ui/index.html
```

---

## Testing

### Backend tests

Run backend tests through the demo setup:

```bash
./scripts/demo/run-demo-test.sh
```

Run backend tests through the development setup:

```bash
./scripts/dev/run-dev-test.sh
```

Manual backend verification can be done with Swagger UI, Postman, or curl. A Postman-friendly request list is included in [Backend testing guide](docs/BACKEND_TESTING.md).

### Frontend tests

Run frontend tests from the `frontend/` directory:

```bash
cd frontend
npm run verify
```

This runs:

- Angular unit/API tests in CI mode
- mocked Playwright E2E tests
- Angular production build

Individual frontend commands:

```bash
npm test
npm run test:ci
npm run e2e
npm run build
```

Frontend testing guide:

```text
docs/FRONTEND_TESTING.md
```

---

## Notes

- This is a demonstration project, not a full warehouse management system.
- The scope is intentionally focused and practical.
- PostgreSQL persistence is managed through Flyway migrations.
- Hibernate validates the schema on startup.
- The backend API is versioned under `/api/v1`.
- Swagger/OpenAPI is included for API exploration.
- Optional demo seed data is separate from normal Flyway migrations.
- Demo and development paths are intentionally separated.
- The frontend can run locally through Angular or as a Dockerized nginx build.
- Mocked Playwright E2E tests verify frontend flows independently of the backend.

---

## Future improvements

Possible future improvements:

- authentication and role-based authorization
- reservation and picking workflows
- supplier and purchase order management
- audit logging
- reporting dashboards
- CI pipeline automation
- Testcontainers-based backend test setup
- real full-stack E2E smoke tests against a live backend
- deployment beyond local Docker usage

---

## License

MIT License

Copyright (c) 2026 Punschkrapferl

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the Software), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.