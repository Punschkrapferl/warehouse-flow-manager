package com.example.warehouseflowmanager.product.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.warehouseflowmanager.WarehouseFlowManagerApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
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

@SpringBootTest(classes = WarehouseFlowManagerApplication.class)
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    private static final String TEST_SKU_PREFIX = "TEST-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void shouldRejectInvalidRecentDaysForReplenishmentCandidates() throws Exception {
        mockMvc.perform(get("/api/products/replenishment-candidates")
                        .param("recentDays", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.recentDays")
                        .value("recentDays must be greater than 0"));
    }

    @Test
    void shouldReturnReplenishmentCandidatesWithBusinessPriorityAndCalculatedQuantities() throws Exception {
        String criticalSku = TEST_SKU_PREFIX + "REPL-CRITICAL-" + UUID.randomUUID();
        String highSku = TEST_SKU_PREFIX + "REPL-HIGH-" + UUID.randomUUID();
        String mediumSku = TEST_SKU_PREFIX + "REPL-MEDIUM-" + UUID.randomUUID();

        long criticalProductId = createProductAndReturnId(criticalSku, "Critical Product", 0, 10, "ACTIVE");
        long highProductId = createProductAndReturnId(highSku, "High Product", 4, 10, "ACTIVE");
        long mediumProductId = createProductAndReturnId(mediumSku, "Medium Product", 10, 10, "ACTIVE");

        // Add recent outbound demand so the endpoint has realistic demand history to evaluate.
        insertOutboundMovement(highProductId, 2, Instant.now().minusSeconds(5L * 24 * 60 * 60));
        insertOutboundMovement(mediumProductId, 3, Instant.now().minusSeconds(3L * 24 * 60 * 60));

        MvcResult result = mockMvc.perform(get("/api/products/replenishment-candidates")
                        .param("recentDays", "30"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

        JsonNode criticalRecommendation = findRecommendationBySku(responseJson, criticalSku);
        JsonNode highRecommendation = findRecommendationBySku(responseJson, highSku);
        JsonNode mediumRecommendation = findRecommendationBySku(responseJson, mediumSku);

        assertNotNull(criticalRecommendation);
        assertNotNull(highRecommendation);
        assertNotNull(mediumRecommendation);

        assertEquals("CRITICAL", criticalRecommendation.get("priority").asText());
        assertEquals(10, criticalRecommendation.get("recommendedReorderQuantity").asInt());
        assertEquals(10, criticalRecommendation.get("shortageQuantity").asInt());
        assertEquals(0, criticalRecommendation.get("recentOutboundQuantity").asInt());

        assertEquals("HIGH", highRecommendation.get("priority").asText());
        assertEquals(8, highRecommendation.get("recommendedReorderQuantity").asInt());
        assertEquals(6, highRecommendation.get("shortageQuantity").asInt());
        assertEquals(2, highRecommendation.get("recentOutboundQuantity").asInt());

        assertEquals("MEDIUM", mediumRecommendation.get("priority").asText());
        assertEquals(3, mediumRecommendation.get("recommendedReorderQuantity").asInt());
        assertEquals(0, mediumRecommendation.get("shortageQuantity").asInt());
        assertEquals(3, mediumRecommendation.get("recentOutboundQuantity").asInt());

        int criticalIndex = findRecommendationIndexBySku(responseJson, criticalSku);
        int highIndex = findRecommendationIndexBySku(responseJson, highSku);
        int mediumIndex = findRecommendationIndexBySku(responseJson, mediumSku);

        // The endpoint should return results in useful operational order.
        assertTrue(criticalIndex >= 0);
        assertTrue(highIndex >= 0);
        assertTrue(mediumIndex >= 0);
        assertTrue(criticalIndex < highIndex);
        assertTrue(highIndex < mediumIndex);

        // Avoid "unused variable" warnings in case you want to inspect them later or extend the test.
        assertTrue(criticalProductId > 0);
    }

    @Test
    void shouldNotReturnRecommendationWhenCalculatedReorderQuantityIsZero() throws Exception {
        String zeroRecommendationSku = TEST_SKU_PREFIX + "REPL-ZERO-" + UUID.randomUUID();

        createProductAndReturnId(zeroRecommendationSku, "Zero Recommendation Product", 0, 0, "ACTIVE");

        MvcResult result = mockMvc.perform(get("/api/products/replenishment-candidates")
                        .param("recentDays", "30"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

        int zeroRecommendationIndex = findRecommendationIndexBySku(responseJson, zeroRecommendationSku);

        // The product still matches the repository candidate query (0 <= 0),
        // but the service should filter it out because the final reorder quantity is 0.
        assertEquals(-1, zeroRecommendationIndex);
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

    private long createProductAndReturnId(
            String sku,
            String name,
            int quantity,
            int minimumQuantity,
            String status
    ) throws Exception {
        String requestBody = """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "Created by replenishment integration test",
                  "unit": "piece",
                  "quantity": %d,
                  "minimumQuantity": %d,
                  "status": "%s"
                }
                """.formatted(sku, name, quantity, minimumQuantity, status);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseJson.get("id").asLong();
    }

    private void insertOutboundMovement(long productId, int quantity, Instant movementAt) {
        Integer currentQuantity = jdbcTemplate.queryForObject("""
                SELECT quantity
                FROM products
                WHERE id = ?
                """, Integer.class, productId);

        jdbcTemplate.update("""
                INSERT INTO stock_movements (
                    product_id,
                    movement_type,
                    quantity,
                    resulting_quantity,
                    note,
                    movement_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                productId,
                "OUTBOUND",
                quantity,
                currentQuantity,
                "Inserted by replenishment integration test",
                Timestamp.from(movementAt)
        );
    }

    private JsonNode findRecommendationBySku(JsonNode responseJson, String sku) {
        if (responseJson == null || !responseJson.isArray()) {
            return null;
        }

        for (JsonNode node : responseJson) {
            JsonNode skuNode = node.get("sku");
            if (skuNode != null && sku.equals(skuNode.asText())) {
                return node;
            }
        }

        return null;
    }

    private int findRecommendationIndexBySku(JsonNode responseJson, String sku) {
        if (responseJson == null || !responseJson.isArray()) {
            return -1;
        }

        for (int index = 0; index < responseJson.size(); index++) {
            JsonNode skuNode = responseJson.get(index).get("sku");
            if (skuNode != null && sku.equals(skuNode.asText())) {
                return index;
            }
        }

        return -1;
    }
}