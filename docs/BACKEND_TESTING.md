# Backend API Test Flow

This document contains a structured manual test flow for the Warehouse Flow Manager backend.

It is intended for:

- manual backend verification
- recruiter or reviewer walkthroughs
- quick backend smoke testing after setup
- demonstrating the main warehouse workflows without needing the frontend

---

## Base URLs

Use the correct base URL depending on how the backend is running:

| Runtime mode           | Base URL                |
|------------------------|-------------------------|
| Local development mode | `http://localhost:8080` |
| Demo mode              | `http://localhost:8081` |

All backend API routes are versioned under:

```text
/api/v1
```

Most examples below use:

```text
http://localhost:8080
```

When testing the Docker demo backend, replace it with:

```text
http://localhost:8081
```

---

## Important note about demo data

The manual create/update flow below is best used with a clean development database.

The demo database may already contain seeded storage locations, products, and stock movements for screenshots and portfolio presentation. If you run this manual flow against the seeded demo database, some create requests may fail because storage location codes or product SKUs already exist.

For the clean manual flow, reset and start the local development environment first:

```bash
./scripts/dev/reset-dev-db.sh
./scripts/dev/run-dev.sh
```

Then use:

```text
http://localhost:8080
```

---

## Optional demo seed data

The demo setup can be populated with realistic warehouse data through:

```bash
./scripts/demo/seed-demo-data.sh
```

The seed data lives in:

```text
src/main/resources/db/demo/V100__seed_demo_data.sql
```

It is intentionally stored under:

```text
db/demo
```

not under:

```text
db/migration
```

Therefore, Flyway does not run it automatically.

The seed script is re-runnable. It deletes and reinserts only the fixed demo records used for portfolio screenshots, not arbitrary business data.

Use the seed data when you want to quickly show:

- dashboard low-stock counts
- replenishment candidates
- product inventory data
- storage location stock overview
- stock movement history
- active, blocked, and discontinued product states

Typical demo flow:

```bash
./scripts/demo/run-demo.sh
./scripts/demo/seed-demo-data.sh
```

Then open:

```text
http://localhost:8081/swagger-ui/index.html
```

---

## Tools you can use

You can test the API with:

- Swagger UI
- Postman
- `curl`

Swagger UI is available after the backend starts.

Local development mode:

```text
http://localhost:8080/swagger-ui/index.html
```

Demo mode:

```text
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
http://localhost:8081/v3/api-docs
```

---

# Backend API Test Flow

This document contains a structured manual test flow for the Warehouse Flow Manager backend.

It is intended for:

- manual backend verification
- quick backend smoke testing after setup
- demonstrating the main warehouse workflows without needing the frontend

---

## Base URLs

Use the correct base URL depending on how the backend is running:

| Runtime mode           | Base URL                |
|------------------------|-------------------------|
| Local development mode | `http://localhost:8080` |
| Demo mode              | `http://localhost:8081` |

All backend API routes are versioned under:

```text
/api/v1
```

Most examples below use:

```text
http://localhost:8080
```

When testing the Docker demo backend, replace it with:

```text
http://localhost:8081
```

---

## Important note about demo data

The manual create/update flow below is best used with a clean development database.

The demo database may already contain seeded storage locations, products, and stock movements for screenshots and portfolio presentation. If you run this manual flow against the seeded demo database, some create requests may fail because storage location codes or product SKUs already exist.

For the clean manual flow, reset and start the local development environment first:

```bash
./scripts/dev/reset-dev-db.sh
./scripts/dev/run-dev.sh
```

Then use:

```text
http://localhost:8080
```

---

## Optional demo seed data

The demo setup can be populated with realistic warehouse data through:

```bash
./scripts/demo/seed-demo-data.sh
```

The seed data lives in:

```text
src/main/resources/db/demo/V100__seed_demo_data.sql
```

It is intentionally stored under:

```text
db/demo
```

not under:

```text
db/migration
```

Therefore, Flyway does not run it automatically.

The seed script is re-runnable. It deletes and reinserts only the fixed demo records used for portfolio screenshots, not arbitrary business data.

Use the seed data when you want to quickly show:

- dashboard low-stock counts
- replenishment candidates
- product inventory data
- storage location stock overview
- stock movement history
- active, blocked, and discontinued product states

Typical demo flow:

```bash
./scripts/demo/run-demo.sh
./scripts/demo/seed-demo-data.sh
```

Then open:

```text
http://localhost:8081/swagger-ui/index.html
```

---

## Tools you can use

You can test the API with:

- Swagger UI
- Postman
- `curl`

Swagger UI is available after the backend starts.

Local development mode:

```text
http://localhost:8080/swagger-ui/index.html
```

Demo mode:

```text
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
http://localhost:8081/v3/api-docs
```

---

## Test scenario overview

This manual test flow covers a realistic warehouse scenario:

1. Health check
2. Create storage location
3. Create product
4. List products
5. Create inbound stock movement
6. Create outbound stock movement
7. Check low-stock products
8. Check replenishment candidates
9. Check product stock movement history
10. Check product stock movement summary
11. Create second storage location
12. Relocate product
13. Check storage location stock overview
14. Try one invalid request
15. Try one business-rule violation

---

## Postman quick reference

For Postman, create a collection variable:

```text
baseUrl
```

Use one of these values:

```text
http://localhost:8080
```

for local development mode, or:

```text
http://localhost:8081
```

for demo mode.

All requests below use:

```text
{{baseUrl}}/api/v1
```

---

### Health

| Method | URL                         |
|--------|-----------------------------|
| `GET`  | `{{baseUrl}}/api/v1/health` |

---

### Products (Demo)

| Method   | URL                                                                                | Purpose                           |
|----------|------------------------------------------------------------------------------------|-----------------------------------|
| `GET`    | `{{baseUrl}}/api/v1/products`                                                      | List products                     |
| `GET`    | `{{baseUrl}}/api/v1/products?page=0&size=10&sortBy=id&direction=asc`               | List products with paging/sorting |
| `GET`    | `{{baseUrl}}/api/v1/products?search=wheel&page=0&size=10&sortBy=id&direction=asc`  | Search products                   |
| `GET`    | `{{baseUrl}}/api/v1/products?status=ACTIVE&page=0&size=10&sortBy=id&direction=asc` | Filter products by status         |
| `GET`    | `{{baseUrl}}/api/v1/products/{id}`                                                 | Get one product                   |
| `POST`   | `{{baseUrl}}/api/v1/products`                                                      | Create product                    |
| `PUT`    | `{{baseUrl}}/api/v1/products/{id}`                                                 | Update product metadata           |
| `PATCH`  | `{{baseUrl}}/api/v1/products/{id}/storage-location`                                | Relocate product                  |
| `DELETE` | `{{baseUrl}}/api/v1/products/{id}`                                                 | Delete product                    |
| `GET`    | `{{baseUrl}}/api/v1/products/low-stock`                                            | Get low-stock products            |
| `GET`    | `{{baseUrl}}/api/v1/products/replenishment-candidates?recentDays=30`               | Get replenishment candidates      |

#### Create product body

```json
{
  "sku": "SKU-POSTMAN-1001",
  "name": "Postman Test Product",
  "description": "Product created from Postman",
  "unit": "piece",
  "quantity": 10,
  "minimumQuantity": 3,
  "status": "ACTIVE",
  "storageLocationId": 1
}
```

#### Update product body

```json
{
  "sku": "SKU-POSTMAN-1001",
  "name": "Updated Postman Test Product",
  "description": "Updated product metadata from Postman",
  "unit": "piece",
  "minimumQuantity": 5,
  "status": "ACTIVE",
  "storageLocationId": 1
}
```

Product quantity is intentionally not changed through the update-product endpoint. Stock quantity changes must go through stock movements.

#### Relocate product body

```json
{
  "storageLocationId": 2
}
```

---

### Storage locations

| Method   | URL                                                        | Purpose                     |
|----------|------------------------------------------------------------|-----------------------------|
| `GET`    | `{{baseUrl}}/api/v1/storage-locations`                     | List storage locations      |
| `GET`    | `{{baseUrl}}/api/v1/storage-locations/{id}`                | Get one storage location    |
| `POST`   | `{{baseUrl}}/api/v1/storage-locations`                     | Create storage location     |
| `PUT`    | `{{baseUrl}}/api/v1/storage-locations/{id}`                | Update storage location     |
| `DELETE` | `{{baseUrl}}/api/v1/storage-locations/{id}`                | Delete storage location     |
| `GET`    | `{{baseUrl}}/api/v1/storage-locations/{id}/stock-overview` | Get location stock overview |

#### Create storage location body

```json
{
  "code": "P-01-01",
  "zone": "POSTMAN-ZONE",
  "description": "Storage location created from Postman",
  "active": true
}
```

#### Update storage location body

```json
{
  "code": "P-01-01",
  "zone": "POSTMAN-ZONE",
  "description": "Updated storage location from Postman",
  "active": true
}
```

---

### Stock movements

| Method | URL                                                        | Purpose                              |
|--------|------------------------------------------------------------|--------------------------------------|
| `GET`  | `{{baseUrl}}/api/v1/stock-movements`                       | List stock movements                 |
| `GET`  | `{{baseUrl}}/api/v1/stock-movements/summary`               | Get stock movement summary           |
| `POST` | `{{baseUrl}}/api/v1/stock-movements`                       | Create stock movement                |
| `GET`  | `{{baseUrl}}/api/v1/products/{id}/stock-movements`         | Get movements for one product        |
| `GET`  | `{{baseUrl}}/api/v1/products/{id}/stock-movements/summary` | Get movement summary for one product |

#### Inbound movement body

```json
{
  "productId": 1,
  "movementType": "INBOUND",
  "quantity": 5,
  "note": "Postman inbound stock movement"
}
```

#### Outbound movement body

```json
{
  "productId": 1,
  "movementType": "OUTBOUND",
  "quantity": 2,
  "note": "Postman outbound stock movement"
}
```

#### Adjustment movement body

```json
{
  "productId": 1,
  "movementType": "ADJUSTMENT",
  "quantity": 20,
  "note": "Postman inventory count adjustment"
}
```

---

## Suggested Postman workflow

A useful Postman test order is:

1. `GET /health`
2. `POST /storage-locations`
3. `POST /products`
4. `GET /products`
5. `POST /stock-movements` with `INBOUND`
6. `POST /stock-movements` with `OUTBOUND`
7. `GET /products/low-stock`
8. `GET /products/replenishment-candidates?recentDays=30`
9. `GET /products/{id}/stock-movements`
10. `GET /products/{id}/stock-movements/summary`
11. `PATCH /products/{id}/storage-location`
12. `GET /storage-locations/{id}/stock-overview`

This gives a reviewer a quick Postman-friendly flow through the main backend features.

---

## 1. Health check

Use this first to confirm the application is running.

### Request

```bash
curl http://localhost:8080/api/v1/health
```

### Example response

```json
{
  "status": "UP",
  "service": "warehouse-flow-manager",
  "timestamp": "2026-04-21T21:00:00Z"
}
```

---

## 2. Create a storage location

### Request

```bash
curl -X POST http://localhost:8080/api/v1/storage-locations \
  -H "Content-Type: application/json" \
  -d '{
    "code": "A-01-01",
    "zone": "ZONE-A",
    "description": "Rack A, aisle 1, level 1",
    "active": true
  }'
```

### Request JSON

```json
{
  "code": "A-01-01",
  "zone": "ZONE-A",
  "description": "Rack A, aisle 1, level 1",
  "active": true
}
```

### Example response

```json
{
  "id": 1,
  "code": "A-01-01",
  "zone": "ZONE-A",
  "description": "Rack A, aisle 1, level 1",
  "active": true
}
```

---

## 3. Create a product

This creates a product and automatically creates an initial `INBOUND` stock movement if `quantity > 0`.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-1001",
    "name": "Industrial Storage Bin",
    "description": "Large plastic bin for warehouse spare parts",
    "unit": "piece",
    "quantity": 10,
    "minimumQuantity": 3,
    "status": "ACTIVE",
    "storageLocationId": 1
  }'
```

### Request JSON

```json
{
  "sku": "SKU-1001",
  "name": "Industrial Storage Bin",
  "description": "Large plastic bin for warehouse spare parts",
  "unit": "piece",
  "quantity": 10,
  "minimumQuantity": 3,
  "status": "ACTIVE",
  "storageLocationId": 1
}
```

### Example response

```json
{
  "id": 1,
  "sku": "SKU-1001",
  "name": "Industrial Storage Bin",
  "description": "Large plastic bin for warehouse spare parts",
  "unit": "piece",
  "quantity": 10,
  "storageLocationId": 1,
  "storageLocationCode": "A-01-01",
  "status": "ACTIVE",
  "minimumQuantity": 3,
  "lowStock": false
}
```

---

## 4. Get all products

### Request

```bash
curl "http://localhost:8080/api/v1/products?page=0&size=10&sortBy=id&direction=asc"
```

### Example response

```json
{
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "content": [
    {
      "id": 1,
      "sku": "SKU-1001",
      "name": "Industrial Storage Bin",
      "description": "Large plastic bin for warehouse spare parts",
      "unit": "piece",
      "quantity": 10,
      "storageLocationId": 1,
      "storageLocationCode": "A-01-01",
      "status": "ACTIVE",
      "minimumQuantity": 3,
      "lowStock": false
    }
  ]
}
```

---

## 5. Create an inbound stock movement

This increases the current quantity.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/stock-movements \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "movementType": "INBOUND",
    "quantity": 5,
    "note": "Restock from supplier"
  }'
```

### Request JSON

```json
{
  "productId": 1,
  "movementType": "INBOUND",
  "quantity": 5,
  "note": "Restock from supplier"
}
```

### Example response

```json
{
  "id": 2,
  "productId": 1,
  "productSku": "SKU-1001",
  "movementType": "INBOUND",
  "quantity": 5,
  "resultingQuantity": 15,
  "note": "Restock from supplier",
  "movementAt": "2026-04-21T21:05:00Z"
}
```

---

## 6. Create an outbound stock movement

This removes stock and must not exceed the current quantity.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/stock-movements \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "movementType": "OUTBOUND",
    "quantity": 13,
    "note": "Order shipment"
  }'
```

### Request JSON

```json
{
  "productId": 1,
  "movementType": "OUTBOUND",
  "quantity": 13,
  "note": "Order shipment"
}
```

### Example response

```json
{
  "id": 3,
  "productId": 1,
  "productSku": "SKU-1001",
  "movementType": "OUTBOUND",
  "quantity": 13,
  "resultingQuantity": 2,
  "note": "Order shipment",
  "movementAt": "2026-04-21T21:07:00Z"
}
```

At this point, the product is considered low stock because `quantity = 2` and `minimumQuantity = 3`.

---

## 7. Get low-stock products

### Request

```bash
curl http://localhost:8080/api/v1/products/low-stock
```

### Example response

```json
[
  {
    "id": 1,
    "sku": "SKU-1001",
    "name": "Industrial Storage Bin",
    "description": "Large plastic bin for warehouse spare parts",
    "unit": "piece",
    "quantity": 2,
    "storageLocationId": 1,
    "storageLocationCode": "A-01-01",
    "status": "ACTIVE",
    "minimumQuantity": 3,
    "lowStock": true
  }
]
```

---

## 8. Get replenishment candidates

This endpoint combines current shortage with recent outbound demand.

### Request

```bash
curl "http://localhost:8080/api/v1/products/replenishment-candidates?recentDays=30"
```

### Example response

```json
[
  {
    "productId": 1,
    "sku": "SKU-1001",
    "name": "Industrial Storage Bin",
    "unit": "piece",
    "currentQuantity": 2,
    "minimumQuantity": 3,
    "shortageQuantity": 1,
    "recentOutboundQuantity": 13,
    "recommendedReorderQuantity": 14,
    "priority": "HIGH",
    "storageLocationId": 1,
    "storageLocationCode": "A-01-01"
  }
]
```

---

## 9. Get product stock movement history

### Request

```bash
curl http://localhost:8080/api/v1/products/1/stock-movements
```

### Example response

```json
[
  {
    "id": 3,
    "productId": 1,
    "productSku": "SKU-1001",
    "movementType": "OUTBOUND",
    "quantity": 13,
    "resultingQuantity": 2,
    "note": "Order shipment",
    "movementAt": "2026-04-21T21:07:00Z"
  },
  {
    "id": 2,
    "productId": 1,
    "productSku": "SKU-1001",
    "movementType": "INBOUND",
    "quantity": 5,
    "resultingQuantity": 15,
    "note": "Restock from supplier",
    "movementAt": "2026-04-21T21:05:00Z"
  },
  {
    "id": 1,
    "productId": 1,
    "productSku": "SKU-1001",
    "movementType": "INBOUND",
    "quantity": 10,
    "resultingQuantity": 10,
    "note": "Initial stock on product creation",
    "movementAt": "2026-04-21T21:00:30Z"
  }
]
```

---

## 10. Get product stock movement summary

### Request

```bash
curl http://localhost:8080/api/v1/products/1/stock-movements/summary
```

### Example response

```json
{
  "productId": 1,
  "productSku": "SKU-1001",
  "from": null,
  "to": null,
  "totalMovements": 3,
  "inboundMovementCount": 2,
  "outboundMovementCount": 1,
  "adjustmentMovementCount": 0,
  "totalInboundQuantity": 15,
  "totalOutboundQuantity": 13,
  "totalAdjustmentQuantity": 0,
  "currentQuantity": 2,
  "latestMovementAt": "2026-04-21T21:07:00Z"
}
```

---

## 11. Create a second storage location

### Request

```bash
curl -X POST http://localhost:8080/api/v1/storage-locations \
  -H "Content-Type: application/json" \
  -d '{
    "code": "B-02-03",
    "zone": "ZONE-B",
    "description": "Rack B, aisle 2, level 3",
    "active": true
  }'
```

### Request JSON

```json
{
  "code": "B-02-03",
  "zone": "ZONE-B",
  "description": "Rack B, aisle 2, level 3",
  "active": true
}
```

### Example response

```json
{
  "id": 2,
  "code": "B-02-03",
  "zone": "ZONE-B",
  "description": "Rack B, aisle 2, level 3",
  "active": true
}
```

---

## 12. Relocate the product

### Request

```bash
curl -X PATCH http://localhost:8080/api/v1/products/1/storage-location \
  -H "Content-Type: application/json" \
  -d '{
    "storageLocationId": 2
  }'
```

### Request JSON

```json
{
  "storageLocationId": 2
}
```

### Example response

```json
{
  "id": 1,
  "sku": "SKU-1001",
  "name": "Industrial Storage Bin",
  "description": "Large plastic bin for warehouse spare parts",
  "unit": "piece",
  "quantity": 2,
  "storageLocationId": 2,
  "storageLocationCode": "B-02-03",
  "status": "ACTIVE",
  "minimumQuantity": 3,
  "lowStock": true
}
```

---

## 13. Get storage location stock overview

### Request

```bash
curl http://localhost:8080/api/v1/storage-locations/2/stock-overview
```

### Example response

```json
{
  "id": 2,
  "code": "B-02-03",
  "zone": "ZONE-B",
  "description": "Rack B, aisle 2, level 3",
  "active": true,
  "totalProducts": 1,
  "totalQuantity": 2,
  "lowStockProductCount": 1,
  "products": [
    {
      "id": 1,
      "sku": "SKU-1001",
      "name": "Industrial Storage Bin",
      "unit": "piece",
      "quantity": 2,
      "minimumQuantity": 3,
      "status": "ACTIVE",
      "lowStock": true
    }
  ]
}
```

---

## 14. Example validation error

This shows how the API responds when a request is invalid.

### Request

```bash
curl "http://localhost:8080/api/v1/products?direction=sideways"
```

### Example response

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

---

## 15. Example business-rule error

This shows a business-rule rejection.

Blocked products cannot be created with initial stock.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-BLOCKED-1",
    "name": "Blocked Product",
    "description": "Should fail",
    "unit": "piece",
    "quantity": 5,
    "minimumQuantity": 0,
    "status": "BLOCKED"
  }'
```

### Example response

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

---

## Quick backend verification commands

Run automated backend tests from the project root.

### Development test setup

Run the automated backend tests through the development setup:

```bash
./scripts/dev/run-dev-test.sh
```

Start the local development environment:

```bash
./scripts/dev/run-dev.sh
```

Stop the development environment:

```bash
./scripts/dev/stop-dev.sh
```

Reset the development database:

```bash
./scripts/dev/reset-dev-db.sh
```

### Demo test setup

Run the demo environment:

```bash
./scripts/demo/run-demo.sh
```

Load optional demo seed data:

```bash
./scripts/demo/seed-demo-data.sh
```

Run the automated backend tests for the demo environment:

```bash
./scripts/demo/run-demo-test.sh
```

Stop the demo environment:

```bash
./scripts/demo/stop-demo.sh
```

Reset the demo database:

```bash
./scripts/demo/reset-demo-db.sh
```

---

## Notes

The backend demonstrates:

- versioned REST API routes under `/api/v1`
- Spring Boot layered architecture
- PostgreSQL persistence
- Flyway database migrations
- DTO-based request and response models
- validation for invalid input
- centralized API error responses
- business-rule checks for stock operations
- integration tests for main API flows
- OpenAPI/Swagger documentation
- Docker-based demo setup

---

## Test scenario overview

This manual test flow covers a realistic warehouse scenario:

1. Health check
2. Create storage location
3. Create product
4. List products
5. Create inbound stock movement
6. Create outbound stock movement
7. Check low-stock products
8. Check replenishment candidates
9. Check product stock movement history
10. Check product stock movement summary
11. Create second storage location
12. Relocate product
13. Check storage location stock overview
14. Try one invalid request
15. Try one business-rule violation

---

## 1. Health check

Use this first to confirm the application is running.

### Request

```bash
curl http://localhost:8080/api/v1/health
```

### Example response

```json
{
  "status": "UP",
  "service": "warehouse-flow-manager",
  "timestamp": "2026-04-21T21:00:00Z"
}
```

---

## 2. Create a storage location

### Request

```bash
curl -X POST http://localhost:8080/api/v1/storage-locations \
  -H "Content-Type: application/json" \
  -d '{
    "code": "A-01-01",
    "zone": "ZONE-A",
    "description": "Rack A, aisle 1, level 1",
    "active": true
  }'
```

### Request JSON

```json
{
  "code": "A-01-01",
  "zone": "ZONE-A",
  "description": "Rack A, aisle 1, level 1",
  "active": true
}
```

### Example response

```json
{
  "id": 1,
  "code": "A-01-01",
  "zone": "ZONE-A",
  "description": "Rack A, aisle 1, level 1",
  "active": true
}
```

---

## 3. Create a product

This creates a product and automatically creates an initial `INBOUND` stock movement if `quantity > 0`.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-1001",
    "name": "Industrial Storage Bin",
    "description": "Large plastic bin for warehouse spare parts",
    "unit": "piece",
    "quantity": 10,
    "minimumQuantity": 3,
    "status": "ACTIVE",
    "storageLocationId": 1
  }'
```

### Request JSON

```json
{
  "sku": "SKU-1001",
  "name": "Industrial Storage Bin",
  "description": "Large plastic bin for warehouse spare parts",
  "unit": "piece",
  "quantity": 10,
  "minimumQuantity": 3,
  "status": "ACTIVE",
  "storageLocationId": 1
}
```

### Example response

```json
{
  "id": 1,
  "sku": "SKU-1001",
  "name": "Industrial Storage Bin",
  "description": "Large plastic bin for warehouse spare parts",
  "unit": "piece",
  "quantity": 10,
  "storageLocationId": 1,
  "storageLocationCode": "A-01-01",
  "status": "ACTIVE",
  "minimumQuantity": 3,
  "lowStock": false
}
```

---

## 4. Get all products

### Request

```bash
curl "http://localhost:8080/api/v1/products?page=0&size=10&sortBy=id&direction=asc"
```

### Example response

```json
{
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "content": [
    {
      "id": 1,
      "sku": "SKU-1001",
      "name": "Industrial Storage Bin",
      "description": "Large plastic bin for warehouse spare parts",
      "unit": "piece",
      "quantity": 10,
      "storageLocationId": 1,
      "storageLocationCode": "A-01-01",
      "status": "ACTIVE",
      "minimumQuantity": 3,
      "lowStock": false
    }
  ]
}
```

---

## 5. Create an inbound stock movement

This increases the current quantity.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/stock-movements \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "movementType": "INBOUND",
    "quantity": 5,
    "note": "Restock from supplier"
  }'
```

### Request JSON

```json
{
  "productId": 1,
  "movementType": "INBOUND",
  "quantity": 5,
  "note": "Restock from supplier"
}
```

### Example response

```json
{
  "id": 2,
  "productId": 1,
  "productSku": "SKU-1001",
  "movementType": "INBOUND",
  "quantity": 5,
  "resultingQuantity": 15,
  "note": "Restock from supplier",
  "movementAt": "2026-04-21T21:05:00Z"
}
```

---

## 6. Create an outbound stock movement

This removes stock and must not exceed the current quantity.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/stock-movements \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "movementType": "OUTBOUND",
    "quantity": 13,
    "note": "Order shipment"
  }'
```

### Request JSON

```json
{
  "productId": 1,
  "movementType": "OUTBOUND",
  "quantity": 13,
  "note": "Order shipment"
}
```

### Example response

```json
{
  "id": 3,
  "productId": 1,
  "productSku": "SKU-1001",
  "movementType": "OUTBOUND",
  "quantity": 13,
  "resultingQuantity": 2,
  "note": "Order shipment",
  "movementAt": "2026-04-21T21:07:00Z"
}
```

At this point, the product is considered low stock because `quantity = 2` and `minimumQuantity = 3`.

---

## 7. Get low-stock products

### Request

```bash
curl http://localhost:8080/api/v1/products/low-stock
```

### Example response

```json
[
  {
    "id": 1,
    "sku": "SKU-1001",
    "name": "Industrial Storage Bin",
    "description": "Large plastic bin for warehouse spare parts",
    "unit": "piece",
    "quantity": 2,
    "storageLocationId": 1,
    "storageLocationCode": "A-01-01",
    "status": "ACTIVE",
    "minimumQuantity": 3,
    "lowStock": true
  }
]
```

---

## 8. Get replenishment candidates

This endpoint combines current shortage with recent outbound demand.

### Request

```bash
curl "http://localhost:8080/api/v1/products/replenishment-candidates?recentDays=30"
```

### Example response

```json
[
  {
    "productId": 1,
    "sku": "SKU-1001",
    "name": "Industrial Storage Bin",
    "unit": "piece",
    "currentQuantity": 2,
    "minimumQuantity": 3,
    "shortageQuantity": 1,
    "recentOutboundQuantity": 13,
    "recommendedReorderQuantity": 14,
    "priority": "HIGH",
    "storageLocationId": 1,
    "storageLocationCode": "A-01-01"
  }
]
```

---

## 9. Get product stock movement history

### Request

```bash
curl http://localhost:8080/api/v1/products/1/stock-movements
```

### Example response

```json
[
  {
    "id": 3,
    "productId": 1,
    "productSku": "SKU-1001",
    "movementType": "OUTBOUND",
    "quantity": 13,
    "resultingQuantity": 2,
    "note": "Order shipment",
    "movementAt": "2026-04-21T21:07:00Z"
  },
  {
    "id": 2,
    "productId": 1,
    "productSku": "SKU-1001",
    "movementType": "INBOUND",
    "quantity": 5,
    "resultingQuantity": 15,
    "note": "Restock from supplier",
    "movementAt": "2026-04-21T21:05:00Z"
  },
  {
    "id": 1,
    "productId": 1,
    "productSku": "SKU-1001",
    "movementType": "INBOUND",
    "quantity": 10,
    "resultingQuantity": 10,
    "note": "Initial stock on product creation",
    "movementAt": "2026-04-21T21:00:30Z"
  }
]
```

---

## 10. Get product stock movement summary

### Request

```bash
curl http://localhost:8080/api/v1/products/1/stock-movements/summary
```

### Example response

```json
{
  "productId": 1,
  "productSku": "SKU-1001",
  "from": null,
  "to": null,
  "totalMovements": 3,
  "inboundMovementCount": 2,
  "outboundMovementCount": 1,
  "adjustmentMovementCount": 0,
  "totalInboundQuantity": 15,
  "totalOutboundQuantity": 13,
  "totalAdjustmentQuantity": 0,
  "currentQuantity": 2,
  "latestMovementAt": "2026-04-21T21:07:00Z"
}
```

---

## 11. Create a second storage location

### Request

```bash
curl -X POST http://localhost:8080/api/v1/storage-locations \
  -H "Content-Type: application/json" \
  -d '{
    "code": "B-02-03",
    "zone": "ZONE-B",
    "description": "Rack B, aisle 2, level 3",
    "active": true
  }'
```

### Request JSON

```json
{
  "code": "B-02-03",
  "zone": "ZONE-B",
  "description": "Rack B, aisle 2, level 3",
  "active": true
}
```

### Example response

```json
{
  "id": 2,
  "code": "B-02-03",
  "zone": "ZONE-B",
  "description": "Rack B, aisle 2, level 3",
  "active": true
}
```

---

## 12. Relocate the product

### Request

```bash
curl -X PATCH http://localhost:8080/api/v1/products/1/storage-location \
  -H "Content-Type: application/json" \
  -d '{
    "storageLocationId": 2
  }'
```

### Request JSON

```json
{
  "storageLocationId": 2
}
```

### Example response

```json
{
  "id": 1,
  "sku": "SKU-1001",
  "name": "Industrial Storage Bin",
  "description": "Large plastic bin for warehouse spare parts",
  "unit": "piece",
  "quantity": 2,
  "storageLocationId": 2,
  "storageLocationCode": "B-02-03",
  "status": "ACTIVE",
  "minimumQuantity": 3,
  "lowStock": true
}
```

---

## 13. Get storage location stock overview

### Request

```bash
curl http://localhost:8080/api/v1/storage-locations/2/stock-overview
```

### Example response

```json
{
  "id": 2,
  "code": "B-02-03",
  "zone": "ZONE-B",
  "description": "Rack B, aisle 2, level 3",
  "active": true,
  "totalProducts": 1,
  "totalQuantity": 2,
  "lowStockProductCount": 1,
  "products": [
    {
      "id": 1,
      "sku": "SKU-1001",
      "name": "Industrial Storage Bin",
      "unit": "piece",
      "quantity": 2,
      "minimumQuantity": 3,
      "status": "ACTIVE",
      "lowStock": true
    }
  ]
}
```

---

## 14. Example validation error

This shows how the API responds when a request is invalid.

### Request

```bash
curl "http://localhost:8080/api/v1/products?direction=sideways"
```

### Example response

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

---

## 15. Example business-rule error

This shows a business-rule rejection.

Blocked products cannot be created with initial stock.

### Request

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-BLOCKED-1",
    "name": "Blocked Product",
    "description": "Should fail",
    "unit": "piece",
    "quantity": 5,
    "minimumQuantity": 0,
    "status": "BLOCKED"
  }'
```

### Example response

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

---

## Quick backend verification commands

Run automated backend tests from the project root.

### Development test setup

Run the automated backend tests through the development setup:

```bash
./scripts/dev/run-dev-test.sh
```

Start the local development environment:

```bash
./scripts/dev/run-dev.sh
```

Stop the development environment:

```bash
./scripts/dev/stop-dev.sh
```

Reset the development database:

```bash
./scripts/dev/reset-dev-db.sh
```

### Demo test setup

Run the demo environment:

```bash
./scripts/demo/run-demo.sh
```

Load optional demo seed data:

```bash
./scripts/demo/seed-demo-data.sh
```

Run the automated backend tests for the demo environment:

```bash
./scripts/demo/run-demo-test.sh
```

Stop the demo environment:

```bash
./scripts/demo/stop-demo.sh
```

Reset the demo database:

```bash
./scripts/demo/reset-demo-db.sh
```

---

## Notes

The backend demonstrates:

- versioned REST API routes under `/api/v1`
- Spring Boot layered architecture
- PostgreSQL persistence
- Flyway database migrations
- DTO-based request and response models
- validation for invalid input
- centralized API error responses
- business-rule checks for stock operations
- integration tests for main API flows
- OpenAPI/Swagger documentation
- Docker-based demo setup