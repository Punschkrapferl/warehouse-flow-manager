package com.example.warehouseflowmanager.product.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.warehouseflowmanager.WarehouseFlowManagerApplication;
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
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = WarehouseFlowManagerApplication.class)
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    private static final String TEST_SKU_PREFIX = "TEST-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldCreateProductWithoutStorageLocationAndCreateInitialStockMovement() throws Exception {
        String sku = TEST_SKU_PREFIX + "PRODUCT-" + UUID.randomUUID();

        String requestBody = """
                {
                  "sku": "%s",
                  "name": "Integration Test Product",
                  "description": "Created by integration test",
                  "unit": "piece",
                  "quantity": 10,
                  "minimumQuantity": 2,
                  "status": "ACTIVE"
                }
                """.formatted(sku);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.name").value("Integration Test Product"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.minimumQuantity").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.storageLocationId").value(nullValue()))
                .andExpect(jsonPath("$.storageLocationCode").value(nullValue()))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        long productId = responseJson.get("id").asLong();

        Long productCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM products
                WHERE id = ?
                  AND sku = ?
                """, Long.class, productId, sku);

        assertNotNull(productCount);
        assertEquals(1L, productCount);

        Long movementCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM stock_movements
                WHERE product_id = ?
                """, Long.class, productId);

        assertNotNull(movementCount);
        assertEquals(1L, movementCount);

        Map<String, Object> movementRow = jdbcTemplate.queryForMap("""
                SELECT movement_type, quantity, resulting_quantity, note
                FROM stock_movements
                WHERE product_id = ?
                """, productId);

        assertEquals("INBOUND", movementRow.get("movement_type"));
        assertEquals(10, ((Number) movementRow.get("quantity")).intValue());
        assertEquals(10, ((Number) movementRow.get("resulting_quantity")).intValue());
        assertEquals("Initial stock on product creation", movementRow.get("note"));
    }

    @Test
    void shouldRejectBlockedProductWithInitialStock() throws Exception {
        String sku = TEST_SKU_PREFIX + "BLOCKED-" + UUID.randomUUID();

        String requestBody = """
                {
                  "sku": "%s",
                  "name": "Blocked Product",
                  "description": "Should not be created",
                  "unit": "piece",
                  "quantity": 5,
                  "minimumQuantity": 0,
                  "status": "BLOCKED"
                }
                """.formatted(sku);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Blocked products cannot be created with initial stock"));

        Long productCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM products
                WHERE sku = ?
                """, Long.class, sku);

        assertNotNull(productCount);
        assertEquals(0L, productCount);
    }

    @Test
    void shouldRejectNegativeProductId() throws Exception {
        mockMvc.perform(get("/api/products/{id}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.id")
                        .value("Product ID must be greater than 0"));
    }

    @Test
    void shouldRejectInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("direction", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.direction")
                        .value("Direction must be either 'asc' or 'desc'"));
    }

    @Test
    void shouldRejectInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.size")
                        .value("Size must be greater than 0"));
    }

    @Test
    void shouldRejectMalformedJsonWhenCreatingProduct() throws Exception {
        String malformedRequestBody = """
                {
                  "sku": "TEST-MALFORMED",
                  "name": "Broken Product",
                  "description": "Malformed JSON test",
                  "unit": "piece",
                  "quantity": 10,
                  "minimumQuantity": 2,
                  "status": "ACTIVE",
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Malformed JSON request or invalid field value"));
    }
}