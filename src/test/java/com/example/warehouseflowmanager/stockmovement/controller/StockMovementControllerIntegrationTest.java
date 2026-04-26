package com.example.warehouseflowmanager.stockmovement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        createProduct(sku, 10);

        Long productId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM products
                WHERE sku = ?
                """, Long.class, sku);

        assertNotNull(productId);

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

        Integer currentQuantity = jdbcTemplate.queryForObject("""
                SELECT quantity
                FROM products
                WHERE id = ?
                """, Integer.class, productId);

        Long movementCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM stock_movements
                WHERE product_id = ?
                """, Long.class, productId);

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
    void shouldRejectOutboundStockMovementWhenQuantityExceedsAvailableStock() throws Exception {
        String sku = TEST_SKU_PREFIX + UUID.randomUUID();

        createProduct(sku, 4);

        Long productId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM products
                WHERE sku = ?
                """, Long.class, sku);

        assertNotNull(productId);

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
                .andExpect(jsonPath("$.message").exists());

        Integer currentQuantity = jdbcTemplate.queryForObject("""
                SELECT quantity
                FROM products
                WHERE id = ?
                """, Integer.class, productId);

        Long movementCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM stock_movements
                WHERE product_id = ?
                """, Long.class, productId);

        assertNotNull(currentQuantity);
        assertNotNull(movementCount);
        assertEquals(4, currentQuantity);
        assertEquals(1L, movementCount);
    }

    private void createProduct(String sku, int quantity) throws Exception {
        String createProductRequest = """
                {
                  "sku": "%s",
                  "name": "Stock Movement Test Product",
                  "description": "Created for stock movement integration tests",
                  "unit": "piece",
                  "quantity": %d,
                  "minimumQuantity": 0,
                  "status": "ACTIVE"
                }
                """.formatted(sku, quantity);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.quantity").value(quantity))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}