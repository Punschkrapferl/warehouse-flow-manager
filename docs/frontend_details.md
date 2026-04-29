# Frontend Details

This document explains the Angular frontend of the Warehouse Flow Manager project.

The frontend is an Angular operations-console application for viewing warehouse products, storage locations, stock movements, low-stock risk, and replenishment candidates.

It uses TypeScript, SCSS, Angular HTTP services, feature facades, unit/API tests, mocked Playwright E2E tests, and an optional Dockerized nginx runtime.

---

## Frontend purpose

The frontend is designed as a practical portfolio UI for the Spring Boot backend.

It demonstrates:

- feature-based Angular structure
- typed REST API integration
- centralized API paths
- centralized API services
- feature facades for state and actions
- frontend error message mapping
- dashboard and table-based operations UI
- loading, empty, and error states
- mocked Playwright E2E tests
- Dockerized nginx production runtime

The frontend does not connect directly to PostgreSQL. All data access goes through the Spring Boot REST API.

---

## Architecture

```text
Angular Frontend
  ↓
App Shell / Router
  ↓
Feature Components
  ↓
Feature Facades
  ↓
Typed API Services
  ↓
Angular HttpClient
  ↓
Spring Boot Backend
  ↓
PostgreSQL Database
````

The frontend communicates with the backend through:

```text
/api/v1
```

---

## Project structure

```text
frontend
├─ e2e
│  ├─ mocks
│  │  └─ warehouse-api.mock.ts
│  ├─ app-shell.spec.ts
│  ├─ dashboard.spec.ts
│  ├─ products.spec.ts
│  ├─ stock-movements.spec.ts
│  ├─ storage-locations.spec.ts
│  └─ tsconfig.json
├─ src
│  ├─ app
│  │  ├─ core
│  │  │  ├─ api
│  │  │  └─ error
│  │  ├─ features
│  │  │  ├─ dashboard
│  │  │  ├─ products
│  │  │  ├─ stock-movements
│  │  │  └─ storage-locations
│  │  ├─ app.component.html
│  │  ├─ app.component.scss
│  │  ├─ app.component.ts
│  │  ├─ app.config.ts
│  │  └─ app.routes.ts
│  ├─ index.html
│  ├─ main.ts
│  ├─ styles.scss
│  ├─ proxy.conf.json
│  └─ proxy.demo.conf.json
├─ Dockerfile
├─ docker-compose.yml
├─ nginx.conf
├─ angular.json
├─ package.json
├─ package-lock.json
├─ playwright.config.ts
└─ tsconfig.json
```

Generated folders such as `dist`, `.angular`, `node_modules`, `playwright-report`, and `test-results` are not part of the source architecture.

---

## App shell and routing

The root app shell provides:

* sidebar navigation
* top navigation area
* runtime mode information
* API target display
* main routed content area

Main feature routes:

```text
/dashboard
/products
/storage-locations
/stock-movements
```

The UI is structured as an operations console rather than a generic CRUD app.

---

## Core frontend layer

Core frontend code lives under:

```text
frontend/src/app/core
```

### API services

API services live in:

```text
frontend/src/app/core/api
```

Main API services:

* `DashboardApiService`
* `ProductsApiService`
* `StorageLocationsApiService`
* `StockMovementsApiService`

API services are responsible for:

* building HTTP requests
* using centralized API paths
* keeping components away from raw endpoint strings
* returning typed response data

### API paths

API path constants live in:

```text
frontend/src/app/core/api/api-paths.ts
```

This keeps frontend API usage consistent with the backend `/api/v1` route structure.

### API models

Shared request and response types live in:

```text
frontend/src/app/core/api/api.types.ts
```

### Error mapping

Frontend error message mapping lives in:

```text
frontend/src/app/core/error/api-error-message.service.ts
```

This keeps backend error display consistent across feature pages.

---

## Feature facades

Each main feature uses a facade for state and action handling.

Facade files:

```text
dashboard.facade.ts
products.facade.ts
stock-movements.facade.ts
storage-locations.facade.ts
```

The facades help keep components focused on rendering while the facade handles:

* loading data
* storing page state
* handling API errors
* triggering refreshes
* preparing data for the template

---

## Dashboard feature

The dashboard shows:

* API health
* low-stock alert count
* critical shortage count
* recent movement count
* low-stock watchlist
* replenishment queue
* recent warehouse movement activity

The dashboard uses backend data from:

```text
/api/v1/health
/api/v1/products/low-stock
/api/v1/products/replenishment-candidates
/api/v1/stock-movements
```

---

## Products feature

The products view shows:

* product ID
* SKU
* item name
* status
* current quantity
* minimum quantity
* stock gap
* unit
* assigned storage location
* low-stock status

The products API supports:

* pagination
* sorting
* filtering
* search
* low-stock detection
* replenishment candidate lookup
* relocation to another storage location

The UI is read-focused and designed for inventory visibility.

---

## Storage locations feature

The storage location view shows:

* active and inactive locations
* location code
* zone
* description
* assigned products
* total quantity per location
* low-stock exposure per location

The detail/overview area shows stock assigned to the selected location.

The frontend calls:

```text
/api/v1/storage-locations
/api/v1/storage-locations/{id}/stock-overview
```

---

## Stock movements feature

The stock movement view shows:

* inbound movements
* outbound movements
* adjustment movements
* product SKU
* movement quantity
* resulting quantity
* movement note
* movement timestamp

The UI gives traceability for stock-changing operations.

The frontend calls:

```text
/api/v1/stock-movements
/api/v1/stock-movements/summary
```

---

## Runtime API flow

The frontend calls the backend through:

```text
/api/v1
```

There are three practical frontend runtime modes:

1. local Angular runtime
2. demo Angular runtime
3. Dockerized frontend runtime

---

### Local Angular runtime

Used during active frontend development.

Command:

```bash
cd frontend
npm start
```

The Angular development server runs on:

```text
http://localhost:4200
```

Proxy file:

```text
frontend/proxy.conf.json
```

The local proxy forwards `/api` requests to the local Spring Boot backend:

```text
http://localhost:8080
```

Flow:

```text
Browser
  ↓
Angular dev server on localhost:4200
  ↓
proxy.conf.json
  ↓
Spring Boot backend on localhost:8080
```

Use this mode when you are developing the frontend against the normal local backend.

---

### Demo Angular runtime

Used when the demo backend is running on `localhost:8081`, but Angular should still run locally with hot reload.

Command:

```bash
cd frontend
npm run start:demo
```

The Angular development server runs on:

```text
http://localhost:4200
```

Proxy file:

```text
frontend/proxy.demo.conf.json
```

The demo proxy forwards `/api` requests to the Docker demo backend:

```text
http://localhost:8081
```

Flow:

```text
Browser
  ↓
Angular dev server on localhost:4200
  ↓
proxy.demo.conf.json
  ↓
Spring Boot demo backend on localhost:8081
```

Use this mode when you want to test frontend changes against the real Docker demo backend before rebuilding the Dockerized frontend image.

This is useful for:

* frontend debugging
* checking real backend responses
* testing seeded demo data
* making UI changes with hot reload
* verifying frontend behavior before creating a production-style Docker build

---

### Dockerized frontend runtime

Used for a production-style demo.

The Angular app is built and served through nginx. The nginx container also proxies `/api/` requests to the demo backend.

Command:

```bash
cd frontend
docker compose up -d --build
```

Open:

```text
http://localhost:4200
```

Main files:

```text
frontend/Dockerfile
frontend/docker-compose.yml
frontend/nginx.conf
```

Flow:

```text
Browser
  ↓
nginx frontend container on localhost:4200
  ↓
nginx /api proxy
  ↓
Spring Boot demo backend on host.docker.internal:8081
```

Use this mode for:

* reviewer walkthroughs
* portfolio screenshots
* production-build verification
* checking nginx routing
* checking the Dockerized frontend image
* showing the frontend with real seeded backend data

The Dockerized frontend serves the Angular production build through nginx and proxies API calls to the demo backend.

---

## Dockerized frontend

The frontend Docker setup uses a multi-stage build.

### Dockerfile behavior

1. Build stage:

   * uses Node Alpine
   * installs dependencies
   * builds the Angular app

2. Runtime stage:

   * uses nginx Alpine
   * copies the Angular production build
   * copies the nginx configuration
   * serves the app on container port `80`

### Docker Compose behavior

The frontend compose file exposes:

```text
localhost:4200 -> container port 80
```

Start the Dockerized frontend:

```bash
cd frontend
docker compose up -d --build
```

Stop the Dockerized frontend:

```bash
cd frontend
docker compose down
```

---

## Styling approach

The frontend uses SCSS.

Global styles live in:

```text
frontend/src/styles.scss
```

Feature styles live next to their components, for example:

```text
dashboard.component.scss
products.component.scss
stock-movements.component.scss
storage-locations.component.scss
```

The visual style is intentionally closer to a warehouse operations console:

* dark sidebar
* dense tables
* status badges
* stock risk indicators
* compact dashboard cards
* clear low-stock and critical indicators

---

## Frontend testing

Frontend testing is documented in:

```text
docs/FRONTEND_TESTING.md
```

The frontend includes:

* Angular unit/API tests
* feature facade tests
* API service tests
* mocked Playwright E2E tests
* production build verification

Run the full frontend verification:

```bash
cd frontend
npm run verify
```

This runs:

* Angular unit/API tests in CI mode
* mocked Playwright E2E tests
* Angular production build

The mocked Playwright tests do not require the Spring Boot backend, PostgreSQL, Docker, or seeded demo data.

---

## Mocked Playwright E2E tests

Playwright tests live in:

```text
frontend/e2e
```

The tests use mocked API responses from:

```text
frontend/e2e/mocks/warehouse-api.mock.ts
```

This allows frontend user flows to be tested without requiring:

* Spring Boot backend
* PostgreSQL
* Docker
* seeded database data

Covered areas include:

* app shell
* dashboard
* products
* stock movements
* storage locations

These tests are useful for verifying frontend behavior quickly and reliably.

They are separate from the manual Dockerized frontend smoke check, which verifies the real frontend container against the real demo backend.

---

## Manual Dockerized frontend smoke check

A manual smoke check can be done when the backend and frontend containers are running together.

Typical flow:

```bash
./scripts/demo/run-demo.sh
./scripts/demo/seed-demo-data.sh

cd frontend
docker compose up -d --build
```

Then open:

```text
http://localhost:4200
```

This verifies that:

* the Angular production build is served by nginx
* Angular routing works through nginx
* `/api/` requests are proxied to the demo backend
* the frontend can display real backend data
* seeded demo data appears correctly in the UI

This is a manual runtime check, not part of the mocked Playwright test suite.

---

## What this frontend demonstrates

This frontend demonstrates:

* feature-based Angular architecture
* typed REST API integration
* centralized API path handling
* centralized frontend error handling
* facade-based state management
* dashboard and table-heavy operations UI
* loading, empty, and error states
* mocked browser-level E2E tests
* production build verification
* Dockerized nginx runtime

