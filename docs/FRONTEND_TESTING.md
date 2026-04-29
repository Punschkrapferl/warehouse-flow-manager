# Frontend Testing

This document explains the frontend testing setup for the Warehouse Flow Manager Angular application.

The frontend tests are separated from backend tests so the project documentation stays clear and easy to review.

Frontend tests cover:

- Angular component behavior
- API service behavior
- facade-driven state handling
- frontend error handling
- mocked browser-level E2E flows with Playwright
- Dockerized frontend smoke testing with Playwright
- production build verification

---

## Frontend location

The Angular frontend is located in:

```text
frontend/
```

Run all frontend commands from this directory unless stated otherwise:

```bash
cd frontend
```

---

## Test types

The frontend currently includes three main automated test levels:

1. **Angular unit/API tests**
2. **Mocked Playwright E2E tests**
3. **Dockerized frontend Playwright smoke test**

The Angular tests check component, facade, and API service behavior.

The mocked Playwright tests check main frontend user flows using mocked backend responses.

The Dockerized frontend smoke test checks the real nginx-served Angular build against the real demo backend through the `/api/` proxy.

A production build check is also included to verify that the Angular application can be compiled successfully.

---

## Why mocked E2E tests?

The normal Playwright tests use mocked API responses instead of requiring the Spring Boot backend to be running.

This has several benefits:

- frontend flows can be tested independently
- tests are faster and more stable
- no database setup is required
- no backend container is required
- no seeded database data is required
- tests are suitable for quick portfolio/reviewer verification

The backend is tested separately through Spring Boot integration tests and manual API smoke tests.

Backend testing is documented in:

```text
BACKEND_TESTING.md
```

The project also includes a separate Dockerized frontend smoke test for checking the real frontend container, nginx proxy, demo backend, and seeded demo data together.

---

## Test file structure

```text
frontend/
├── e2e/
│   ├── mocks/
│   │   └── warehouse-api.mock.ts
│   ├── app-shell.spec.ts
│   ├── dashboard.spec.ts
│   ├── products.spec.ts
│   ├── stock-movements.spec.ts
│   ├── storage-locations.spec.ts
│   └── tsconfig.json
├── e2e-docker/
│   └── docker-smoke.spec.ts
├── src/
│   └── app/
│       ├── core/
│       │   ├── api/
│       │   │   ├── dashboard-api.service.spec.ts
│       │   │   ├── products-api.service.spec.ts
│       │   │   ├── stock-movements-api.service.spec.ts
│       │   │   └── storage-locations-api.service.spec.ts
│       │   └── error/
│       │       └── api-error-message.service.ts
│       └── features/
│           ├── dashboard/
│           │   ├── dashboard.component.spec.ts
│           │   └── dashboard.facade.ts
│           ├── products/
│           │   ├── products.component.spec.ts
│           │   └── products.facade.ts
│           ├── stock-movements/
│           │   ├── stock-movements.component.spec.ts
│           │   └── stock-movements.facade.ts
│           └── storage-locations/
│               ├── storage-locations.component.spec.ts
│               └── storage-locations.facade.ts
├── playwright.config.ts
├── playwright.docker.config.ts
├── package.json
└── angular.json
```

Generated folders such as `test-results`, `playwright-report`, `playwright-report-docker`, `dist`, `.angular`, and `node_modules` are not part of the source test structure.

---

## Package scripts

The frontend provides these relevant npm scripts:

```bash
npm start
npm run start:demo
npm test
npm run test:ci
npm run e2e
npm run e2e:docker
npm run e2e:ui
npm run e2e:headed
npm run e2e:debug
npm run build
npm run verify
```

The recommended one-command frontend verification is:

```bash
npm run verify
```

The Dockerized frontend smoke test is run separately:

```bash
npm run e2e:docker
```

---

## 1. Angular unit/API tests

Run:

```bash
npm test
```

These tests verify the Angular application logic.

Covered areas include:

- component rendering
- component state handling
- facade interaction
- API service request paths
- API service query parameters
- frontend error message mapping
- loading states
- empty states
- error states

`npm test` runs in watch mode.

After the tests pass, quit the watch process with:

```text
q
```

### CI-style Angular test run

Run:

```bash
npm run test:ci
```

This runs the Angular unit/API tests once without watch mode.

It is useful for:

- automated verification
- local pre-commit checks
- CI pipelines
- reviewer-friendly test execution

---

## 2. Mocked Playwright E2E tests

Run:

```bash
npm run e2e
```

The Playwright tests start the Angular app automatically and test the main frontend workflows.

The tests use mocked API responses from:

```text
frontend/e2e/mocks/warehouse-api.mock.ts
```

This means the E2E tests do not require:

- Spring Boot backend
- PostgreSQL
- Docker
- seeded database data

---

## Mocked E2E test coverage

### App shell

File:

```text
frontend/e2e/app-shell.spec.ts
```

Covers:

- application shell renders
- navigation layout is visible
- main routes are reachable

### Dashboard

File:

```text
frontend/e2e/dashboard.spec.ts
```

Covers:

- dashboard page renders
- KPI cards are visible
- low-stock/replenishment-related dashboard data is displayed
- recent stock movement information is shown

### Products

File:

```text
frontend/e2e/products.spec.ts
```

Covers:

- products page renders
- product table/list is visible
- mocked product data is displayed
- product filtering/search UI can be tested independently of backend data

### Stock movements

File:

```text
frontend/e2e/stock-movements.spec.ts
```

Covers:

- stock movements page renders
- movement data is displayed
- movement summary/KPI information is visible
- filters can be verified against mocked responses

### Storage locations

File:

```text
frontend/e2e/storage-locations.spec.ts
```

Covers:

- storage locations page renders
- storage location list is visible
- selected location overview is displayed
- location stock overview is shown with mocked data

---

## 3. Dockerized frontend smoke test

The project includes a separate Playwright smoke test for the real Dockerized frontend runtime.

Run:

```bash
npm run e2e:docker
```

This command uses:

```text
frontend/playwright.docker.config.ts
```

and tests:

```text
frontend/e2e-docker/docker-smoke.spec.ts
```

The Docker smoke test assumes that the demo backend and Dockerized frontend are already running.

Typical setup from the project root:

```bash
./scripts/demo/run-demo.sh
./scripts/demo/seed-demo-data.sh
```

Then start the Dockerized frontend:

```bash
cd frontend
docker compose up -d --build
```

Then run the Docker smoke test:

```bash
npm run e2e:docker
```

The Docker smoke test checks that:

- the Dockerized Angular frontend is reachable on `http://localhost:4200`
- nginx serves the Angular production build
- nginx proxies `/api/` requests to the demo backend
- `/api/v1/health` is reachable through the frontend container
- seeded demo products are reachable through the real API path
- main frontend routes load without API failures

This test is intentionally separate from `npm run verify` because it depends on Docker, the demo backend, the frontend container, database state, and seeded demo data.

---

## 4. Playwright UI mode

Run:

```bash
npm run e2e:ui
```

Use this when you want to inspect tests visually through Playwright's UI runner.

This is useful for:

- debugging selectors
- checking page rendering
- inspecting failed tests
- reviewing mocked E2E flows interactively

---

## 5. Headed browser mode

Run:

```bash
npm run e2e:headed
```

This runs Playwright tests in a visible browser window.

Use it when you want to see the browser actions while tests run.

---

## 6. Debug mode

Run:

```bash
npm run e2e:debug
```

Use this when you need step-by-step debugging for a failing E2E test.

---

## 7. Production build check

Run:

```bash
npm run build
```

This verifies that the Angular application can be built successfully for production.

The build check is useful because it catches:

- TypeScript errors
- template errors
- invalid imports
- routing issues
- SCSS build errors
- production build issues not always visible during development

---

## Recommended frontend verification flow

Before committing normal frontend changes, run:

```bash
npm run verify
```

This command runs:

- Angular unit/API tests in CI mode
- mocked Playwright E2E tests
- Angular production build

For interactive local unit test development, use:

```bash
npm test
```

Because `npm test` runs in watch mode, quit it with:

```text
q
```

For a real Docker runtime check, run the Dockerized frontend smoke test separately:

```bash
npm run e2e:docker
```

---

## Recommended reviewer flow

A reviewer can verify the frontend in two levels.

### Fast frontend verification

From the `frontend/` directory:

```bash
npm install
npm run verify
```

This verifies the frontend code without requiring Docker, PostgreSQL, or the Spring Boot backend.

### Real Docker runtime verification

From the project root:

```bash
cp .env.example .env
./scripts/demo/run-demo.sh
./scripts/demo/seed-demo-data.sh
```

Then:

```bash
cd frontend
docker compose up -d --build
npm run e2e:docker
```

This verifies the Dockerized frontend against the real demo backend and seeded demo data.

---

## Expected result

A successful normal frontend verification should show:

```text
Angular unit/API tests passing
Mocked Playwright E2E tests passing
Angular production build successful
```

A successful Dockerized frontend smoke test should show:

```text
Dockerized frontend reachable
Backend health endpoint reachable through nginx proxy
Seeded product data reachable through nginx proxy
Main frontend routes load successfully
No unexpected API failures
```

The production build should complete without TypeScript, template, routing, or SCSS build errors.

---

## Notes about ports

The default Angular development server usually runs on:

```text
http://localhost:4200
```

For mocked Playwright tests, the project can use another port such as:

```text
http://localhost:4300
```

This avoids conflicts when port `4200` is already used by another process.

The normal Playwright configuration handles starting the Angular app automatically for mocked E2E tests.

The Dockerized frontend smoke test uses the already-running frontend container on:

```text
http://localhost:4200
```

Therefore, port `4200` must be available for the Dockerized frontend container.

---

## Notes about backend targets

The local Angular development setup uses:

```text
frontend/proxy.conf.json
```

This forwards frontend API calls to the local backend, usually:

```text
http://localhost:8080
```

The demo Angular setup uses:

```text
frontend/proxy.demo.conf.json
```

This forwards frontend API calls to the demo backend, usually:

```text
http://localhost:8081
```

The Dockerized frontend runtime uses nginx:

```text
frontend/nginx.conf
```

This proxies `/api/` calls from the frontend container to the demo backend.

The mocked Playwright E2E tests do not require any backend target, because API responses are mocked in the browser test layer.

The Dockerized frontend smoke test does require the real demo backend because it verifies the nginx `/api/` proxy and real API-backed frontend behavior.

---

## Notes about mocked API responses

The mocked API data lives in:

```text
frontend/e2e/mocks/warehouse-api.mock.ts
```

This file provides stable test responses for the main frontend pages.

When the frontend UI changes, the mocks may need to be updated so the E2E tests continue to represent realistic warehouse data.

---

## Dockerized frontend smoke check

The frontend can be verified through the Dockerized nginx runtime.

Start the demo backend first:

```bash
./scripts/demo/run-demo.sh
```

Optionally load demo data:

```bash
./scripts/demo/seed-demo-data.sh
```

Then start the Dockerized frontend:

```bash
cd frontend
docker compose up -d --build
```

Open:

```text
http://localhost:4200
```

Run the automated Docker smoke test:

```bash
npm run e2e:docker
```

This check verifies that:

- the Angular production build is served by nginx
- Angular routing works through nginx
- `/api/` requests are proxied to the demo backend
- the frontend can display real seeded backend data when the demo database has been populated

Stop the Dockerized frontend with:

```bash
docker compose down
```

This is a real runtime smoke test and is separate from the mocked Playwright E2E test suite.

---

## What these tests do not cover

The mocked frontend E2E tests do not verify the real backend or database.

They do not test:

- real Spring Boot endpoints
- real PostgreSQL data
- Flyway migrations
- backend business rules
- backend error handling
- nginx Docker proxy behavior

Those areas are covered separately by:

- backend integration tests
- backend manual API smoke tests
- Swagger/Postman/curl verification
- Dockerized frontend smoke tests
- manual Dockerized frontend checks

See:

```text
BACKEND_TESTING.md
```

The Dockerized frontend smoke test verifies that the frontend container and nginx proxy work with the real demo backend, but it is intentionally small. It is not a full end-to-end business workflow test.

---

## Optional future improvement

A useful future addition would be one larger real full-stack Playwright test that performs a complete business workflow against the live backend.

For example:

1. Start PostgreSQL and Spring Boot.
2. Start the Dockerized frontend.
3. Open the app with Playwright.
4. Create or verify a storage location.
5. Create or verify a product.
6. Verify stock movements and dashboard updates.

This is not required for the current portfolio milestone because the backend, frontend, and Docker runtime are already verified through separate focused test layers.