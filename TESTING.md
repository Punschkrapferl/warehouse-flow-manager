# Manual API Test Flow

This document contains a structured manual test flow for the Warehouse Flow Manager backend.

It is intended for:

- manual backend verification
- recruiter or reviewer walkthroughs
- quick smoke testing after setup
- demonstrating the main warehouse workflows without needing a frontend

---

## Base URLs

Use the correct base URL depending on how the backend is running:

- **Local dev mode:** `http://localhost:8080`
- **Demo mode:** `http://localhost:8081`

In all examples below, replace the base URL if needed.

---

## Tools You Can Use

You can test the API with:

- Swagger UI
- Postman
- `curl`

---

## Test Scenario Overview

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

## 1. Health Check

Use this first to confirm the application is running.

### Request

```bash
curl http://localhost:8080/api/v1/health
````

### Example Response

```json
{
  "status": "UP",
  "service": "warehouse-flow-manager",
  "timestamp": "2026-04-21T21:00:00Z"
}
```

---

## 2. Create a Storage Location

### Request

```bash
curl -X POST http://localhost:8080/api/storage-locations \
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

### Example Response

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

## 3. Create a Product

This creates a product and automatically creates an initial `INBOUND` stock movement if `quantity > 0`.

### Request

```bash
curl -X POST http://localhost:8080/api/products \
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

### Example Response

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

## 4. Get All Products

### Request

```bash
curl "http://localhost:8080/api/products?page=0&size=10&sortBy=id&direction=asc"
```

### Example Response

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

## 5. Create an Inbound Stock Movement

This increases the current quantity.

### Request

```bash
curl -X POST http://localhost:8080/api/stock-movements \
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

### Example Response

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

## 6. Create an Outbound Stock Movement

This removes stock and must not exceed the current quantity.

### Request

```bash
curl -X POST http://localhost:8080/api/stock-movements \
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

### Example Response

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

At this point, the product will likely be considered low stock because `quantity = 2` and `minimumQuantity = 3`.

---

## 7. Get Low-Stock Products

### Request

```bash
curl http://localhost:8080/api/products/low-stock
```

### Example Response

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

## 8. Get Replenishment Candidates

This endpoint combines current shortage with recent outbound demand.

### Request

```bash
curl "http://localhost:8080/api/products/replenishment-candidates?recentDays=30"
```

### Example Response

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

## 9. Get Product Stock Movement History

### Request

```bash
curl http://localhost:8080/api/products/1/stock-movements
```

### Example Response

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
    "note": "Initial stock on products creation",
    "movementAt": "2026-04-21T21:00:30Z"
  }
]
```

---

## 10. Get Product Stock Movement Summary

### Request

```bash
curl http://localhost:8080/api/products/1/stock-movements/summary
```

### Example Response

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

## 11. Create a Second Storage Location

### Request

```bash
curl -X POST http://localhost:8080/api/storage-locations \
  -H "Content-Type: application/json" \
  -d '{
    "code": "B-02-03",
    "zone": "ZONE-B",
    "description": "Rack B, aisle 2, level 3",
    "active": true
  }'
```

### Example Response

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

## 12. Relocate the Product

### Request

```bash
curl -X PATCH http://localhost:8080/api/products/1/storage-location \
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

### Example Response

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

## 13. Get Storage Location Stock Overview

### Request

```bash
curl http://localhost:8080/api/storage-locations/2/stock-overview
```

### Example Response

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

## Example Validation Error

This shows how the API responds when a request is invalid.

### Request

```bash
curl "http://localhost:8080/api/products?direction=sideways"
```

### Example Response

```json
{
  "timestamp": "2026-04-21T21:10:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "validationErrors": {
    "direction": "Direction must be either 'asc' or 'desc'"
  }
}
```

---

## Example Business Rule Error

This shows a business-rule rejection.

### Request

```bash
curl -X POST http://localhost:8080/api/products \
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

### Example Response

```json
{
  "timestamp": "2026-04-21T21:12:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Blocked products cannot be created with initial stock",
  "path": "/api/products",
  "validationErrors": null
}
```








