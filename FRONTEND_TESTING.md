# Frontend Testing

This document explains the frontend testing setup for the Warehouse Flow Manager Angular application.

The frontend tests are separated from backend tests so the project documentation stays clear and easy to review.

Frontend tests cover:

- Angular component behavior
- API service behavior
- facade-driven state handling
- frontend error handling
- mocked browser-level E2E flows with Playwright

---

## Frontend Location

The Angular frontend is located in:

```text
frontend/
```

Run all frontend commands from this directory unless stated otherwise:

```bash
cd frontend
```

---

## Test Types

The frontend currently includes two main test levels:

1. **Angular unit/API tests**
2. **Mocked Playwright E2E tests**

The Angular tests check component, facade, and API service behavior.

The Playwright tests check main user flows in a browser-like environment using mocked backend responses.

---

## Why Mocked E2E Tests?

The Playwright tests use mocked API responses instead of requiring the Spring Boot backend to be running.

This has several benefits:

- frontend flows can be tested independently
- tests are faster and more stable
- no database setup is required
- no backend container is required
- the tests are suitable for quick portfolio/reviewer verification

The backend is tested separately through Spring Boot integration tests and manual API smoke tests.

Backend testing is documented in:

```text
BACKEND_TESTING.md
```

---

## Test File Structure

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
├── src/
│   └── app/
│       ├── core/
│       │   ├── api/
│       │   │   ├── dashboard-api.service.spec.ts
│       │   │   ├── products-api.service.spec.ts
│       │   │   ├── stock-movements-api.service.spec.ts
│       │   │   └── storage-locations-api.service.spec.ts
│       │   └── error/
│       └── features/
│           ├── dashboard/
│           │   └── dashboard.component.spec.ts
│           ├── products/
│           │   └── products.component.spec.ts
│           ├── stock-movements/
│           │   └── stock-movements.component.spec.ts
│           └── storage-locations/
│               └── storage-locations.component.spec.ts
├── playwright.config.ts
├── package.json
└── angular.json
```

---

## Package Scripts

The frontend provides the following relevant npm scripts:

```bash
npm start
npm run start:demo
npm test
npm run test:ci
npm run e2e
npm run e2e:ui
npm run e2e:headed
npm run e2e:debug
npm run build
npm run verify
```

The recommended one-command frontend check is:

```bash
npm run verify
``` 

---

## 1. Angular Unit/API Tests

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
- loading and error states

`npm test` runs in watch mode.

After the tests pass, quit the watch process with:

```text
q
```

### CI-Style Angular Test Run

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

## 2. Mocked Playwright E2E Tests

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

## E2E Test Coverage

### App shell

File:

```text
frontend/e2e/app-shell.spec.ts
```

Covers:

- application shell renders
- navigation layout is visible
- main routes are reachable

---

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

---

### Products

File:

```text
frontend/e2e/products.spec.ts
```

Covers:

- products page renders
- product table/list is visible
- mocked product data is displayed
- product filtering/search UI can be tested independently from backend data

---

### Stock Movements

File:

```text
frontend/e2e/stock-movements.spec.ts
```

Covers:

- stock movements page renders
- movement data is displayed
- movement summary/KPI information is visible
- filters can be verified against mocked responses

---

### Storage Locations

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

## 3. Playwright UI Mode

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

## 4. Headed Browser Mode

Run:

```bash
npm run e2e:headed
```

This runs Playwright tests in a visible browser window.

Use it when you want to see the browser actions while tests run.

---

## 5. Debug Mode

Run:

```bash
npm run e2e:debug
```

Use this when you need step-by-step debugging for a failing E2E test.

---

## 6. Production Build Check

Run:

```bash
npm run build
```

This verifies that the Angular application can be built successfully for production.

The build check is useful because it catches:

- TypeScript errors
- template errors
- invalid imports
- SCSS budget warnings/errors
- production build issues not always visible during development

---

## Recommended Frontend Verification Flow

Before committing frontend changes, run:

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

---

## Expected Result

A successful frontend verification should show:

```text
Angular unit/API tests passing
Playwright E2E tests passing
Angular production build successful
```

The production build should complete without component style budget warnings.

---

## Notes About Ports

The default Angular development server usually runs on:

```text
http://localhost:4200
```

For Playwright, the project can use another port such as:

```text
http://localhost:4300
```

This avoids conflicts when port `4200` is already used by another process.

The Playwright configuration handles starting the Angular app automatically for E2E tests.

---

## Notes About Mocked API Responses

The mocked API data lives in:

```text
frontend/e2e/mocks/warehouse-api.mock.ts
```

This file provides stable test responses for the main frontend pages.

When the frontend UI changes, the mocks may need to be updated so the E2E tests continue to represent realistic warehouse data.

---

## What These Tests Do Not Cover

The mocked frontend E2E tests do not verify the real backend or database.

They do not test:

- real Spring Boot endpoints
- real PostgreSQL data
- Flyway migrations
- backend business rules
- backend error handling

Those areas are covered separately by:

- backend integration tests
- backend manual API smoke tests
- Swagger/Postman/curl verification

See:

```text
BACKEND_TESTING.md
```

---

## Optional Future Improvement

A useful future addition would be one small real full-stack smoke test that runs against the live Spring Boot backend.

For example:

1. Start PostgreSQL and Spring Boot
2. Start Angular
3. Open the app with Playwright
4. Verify one real API-backed page loads successfully

This is not required for the current portfolio milestone because the backend and frontend are already tested separately.