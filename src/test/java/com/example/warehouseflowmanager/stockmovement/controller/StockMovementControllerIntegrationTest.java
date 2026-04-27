package com.example.warehouseflowmanager.stockmovement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.warehouseflowmanager.WarehouseFlowManagerApplication;
import com.example.warehouseflowmanager.common.api.ApiPaths;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = WarehouseFlowManagerApplication.class)
@AutoConfigureMockMvc
class StockMovementControllerIntegrationTest {

    private static final String TEST_SKU_PREFIX = "TEST-MOVE-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanUpTestData() {
        jdbcTemplate.update("""
                DELETE FROM stock_movements
                WHERE product_id IN (
                    SELECT id
                    FROM products
                    WHERE sku LIKE ?
                )
                """, TEST_SKU_PREFIX + "%");

        jdbcTemplate.update("""
                DELETE FROM products
                WHERE sku LIKE ?
                """, TEST_SKU_PREFIX + "%");
    }

    @Test
    void shouldCreateInboundStockMovementAndIncreaseProductQuantity() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 10, "ACTIVE");

        Long productId = findProductIdBySku(sku);

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "INBOUND",
                  "quantity": 5,
                  "note": "Restock from supplier"
                }
                """.formatted(productId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productSku").value(sku))
                .andExpect(jsonPath("$.movementType").value("INBOUND"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.resultingQuantity").value(15))
                .andExpect(jsonPath("$.note").value("Restock from supplier"));

        Integer currentQuantity = getProductQuantity(productId);
        Long movementCount = countStockMovements(productId);

        Map<String, Object> latestMovement = jdbcTemplate.queryForMap("""
                SELECT movement_type, quantity, resulting_quantity, note
                FROM stock_movements
                WHERE product_id = ?
                ORDER BY id DESC
                LIMIT 1
                """, productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(15, currentQuantity);
        assertEquals(2L, movementCount);
        assertEquals("INBOUND", latestMovement.get("movement_type"));
        assertEquals(5, ((Number) latestMovement.get("quantity")).intValue());
        assertEquals(15, ((Number) latestMovement.get("resulting_quantity")).intValue());
        assertEquals("Restock from supplier", latestMovement.get("note"));
    }

    @Test
    void shouldCreateOutboundStockMovementAndDecreaseProductQuantity() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 10, "ACTIVE");

        Long productId = findProductIdBySku(sku);

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "OUTBOUND",
                  "quantity": 4,
                  "note": "Picked for customer order"
                }
                """.formatted(productId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productSku").value(sku))
                .andExpect(jsonPath("$.movementType").value("OUTBOUND"))
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.resultingQuantity").value(6))
                .andExpect(jsonPath("$.note").value("Picked for customer order"));

        Integer currentQuantity = getProductQuantity(productId);
        Long movementCount = countStockMovements(productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(6, currentQuantity);
        assertEquals(2L, movementCount);
    }

    @Test
    void shouldCreateAdjustmentStockMovementAndSetProductQuantity() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 10, "ACTIVE");

        Long productId = findProductIdBySku(sku);

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "ADJUSTMENT",
                  "quantity": 3,
                  "note": "Inventory count correction"
                }
                """.formatted(productId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productSku").value(sku))
                .andExpect(jsonPath("$.movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.resultingQuantity").value(3))
                .andExpect(jsonPath("$.note").value("Inventory count correction"));

        Integer currentQuantity = getProductQuantity(productId);
        Long movementCount = countStockMovements(productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(3, currentQuantity);
        assertEquals(2L, movementCount);
    }

    @Test
    void shouldRejectOutboundStockMovementWhenQuantityExceedsAvailableStock() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 4, "ACTIVE");

        Long productId = findProductIdBySku(sku);

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "OUTBOUND",
                  "quantity": 10,
                  "note": "Attempt to remove more than available"
                }
                """.formatted(productId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Outbound quantity cannot exceed current stock"));

        Integer currentQuantity = getProductQuantity(productId);
        Long movementCount = countStockMovements(productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(4, currentQuantity);
        assertEquals(1L, movementCount);
    }

    @Test
    void shouldRejectStockMovementForBlockedProduct() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 0, "BLOCKED");

        Long productId = findProductIdBySku(sku);

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "INBOUND",
                  "quantity": 5,
                  "note": "Attempt to move blocked product"
                }
                """.formatted(productId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Blocked products cannot be moved"));

        Integer currentQuantity = getProductQuantity(productId);
        Long movementCount = countStockMovements(productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(0, currentQuantity);
        assertEquals(0L, movementCount);
    }

    @Test
    void shouldReturnNotFoundWhenCreatingStockMovementForMissingProduct() throws Exception {
        long missingProductId = 999999999L;

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "INBOUND",
                  "quantity": 5,
                  "note": "Missing product"
                }
                """.formatted(missingProductId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: " + missingProductId));
    }

    @Test
    void shouldRejectInboundStockMovementWithZeroQuantity() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 0, "ACTIVE");

        Long productId = findProductIdBySku(sku);

        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "INBOUND",
                  "quantity": 0,
                  "note": "Invalid zero inbound quantity"
                }
                """.formatted(productId);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("INBOUND quantity must be greater than 0"));

        Integer currentQuantity = getProductQuantity(productId);
        Long movementCount = countStockMovements(productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(0, currentQuantity);
        assertEquals(0L, movementCount);
    }

    @Test
    void shouldReturnStockMovementSummaryForProduct() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 10, "ACTIVE");

        Long productId = findProductIdBySku(sku);

        createStockMovement(productId, "INBOUND", 5, "Restock");
        createStockMovement(productId, "OUTBOUND", 3, "Customer order");
        createStockMovement(productId, "ADJUSTMENT", 7, "Inventory correction");

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}/stock-movements/summary", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productSku").value(sku))
                .andExpect(jsonPath("$.totalMovements").value(4))
                .andExpect(jsonPath("$.inboundMovementCount").value(2))
                .andExpect(jsonPath("$.outboundMovementCount").value(1))
                .andExpect(jsonPath("$.adjustmentMovementCount").value(1))
                .andExpect(jsonPath("$.totalInboundQuantity").value(15))
                .andExpect(jsonPath("$.totalOutboundQuantity").value(3))
                .andExpect(jsonPath("$.totalAdjustmentQuantity").value(7))
                .andExpect(jsonPath("$.currentQuantity").value(7));
    }

    @Test
    void shouldRejectInvalidDateRangeForStockMovementQuery() throws Exception {
        mockMvc.perform(get(ApiPaths.STOCK_MOVEMENTS)
                        .param("from", "2026-04-12T18:00:00Z")
                        .param("to", "2026-04-12T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("'from' must be before or equal to 'to'"));
    }

    @Test
    void shouldReturnNotFoundWhenQueryingStockMovementsForMissingProduct() throws Exception {
        long missingProductId = 999999999L;

        mockMvc.perform(get(ApiPaths.STOCK_MOVEMENTS)
                        .param("productId", String.valueOf(missingProductId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: " + missingProductId));
    }

    private void createProduct(String sku, int quantity, String status) throws Exception {
        String createProductRequest = """
                {
                  "sku": "%s",
                  "name": "Stock Movement Test Product",
                  "description": "Created for stock movement integration tests",
                  "unit": "piece",
                  "quantity": %d,
                  "minimumQuantity": 0,
                  "status": "%s"
                }
                """.formatted(sku, quantity, status);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.quantity").value(quantity))
                .andExpect(jsonPath("$.status").value(status));
    }

    private void createStockMovement(
            Long productId,
            String movementType,
            int quantity,
            String note
    ) throws Exception {
        String stockMovementRequest = """
                {
                  "productId": %d,
                  "movementType": "%s",
                  "quantity": %d,
                  "note": "%s"
                }
                """.formatted(productId, movementType, quantity, note);

        mockMvc.perform(post(ApiPaths.STOCK_MOVEMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockMovementRequest))
                .andExpect(status().isCreated());
    }

    private Long findProductIdBySku(String sku) {
        Long productId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM products
                WHERE sku = ?
                """, Long.class, sku);

        assertNotNull(productId);
        return productId;
    }

    private Integer getProductQuantity(Long productId) {
        return jdbcTemplate.queryForObject("""
                SELECT quantity
                FROM products
                WHERE id = ?
                """, Integer.class, productId);
    }

    private Long countStockMovements(Long productId) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM stock_movements
                WHERE product_id = ?
                """, Long.class, productId);
    }
}