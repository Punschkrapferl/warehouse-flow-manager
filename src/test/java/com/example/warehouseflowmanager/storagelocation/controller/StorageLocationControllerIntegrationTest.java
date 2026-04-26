package com.example.warehouseflowmanager.storagelocation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.warehouseflowmanager.WarehouseFlowManagerApplication;
import com.example.warehouseflowmanager.common.api.ApiPaths;
import java.util.Locale;
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
class StorageLocationControllerIntegrationTest {

    private static final String TEST_LOCATION_CODE_PREFIX = "TEST-LOC-";
    private static final String TEST_PRODUCT_SKU_PREFIX = "TEST-LOC-PROD-";

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
                """, TEST_PRODUCT_SKU_PREFIX + "%");

        jdbcTemplate.update("""
                DELETE FROM products
                WHERE sku LIKE ?
                """, TEST_PRODUCT_SKU_PREFIX + "%");

        jdbcTemplate.update("""
                DELETE FROM storage_locations
                WHERE code LIKE ?
                """, TEST_LOCATION_CODE_PREFIX + "%");
    }

    @Test
    void shouldRejectDeletingStorageLocationWhenProductsAreAssigned() throws Exception {
        String locationCode = TEST_LOCATION_CODE_PREFIX + UUID.randomUUID()
                .toString()
                .toUpperCase(Locale.ROOT);

        String productSku = TEST_PRODUCT_SKU_PREFIX + UUID.randomUUID();

        String createLocationRequest = """
                {
                  "code": "%s",
                  "zone": "ZONE-T",
                  "description": "Integration test storage location",
                  "active": true
                }
                """.formatted(locationCode);

        mockMvc.perform(post(ApiPaths.STORAGE_LOCATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLocationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(locationCode))
                .andExpect(jsonPath("$.zone").value("ZONE-T"))
                .andExpect(jsonPath("$.active").value(true));

        Long storageLocationId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM storage_locations
                WHERE code = ?
                """, Long.class, locationCode);

        assertNotNull(storageLocationId);

        String createProductRequest = """
                {
                  "sku": "%s",
                  "name": "Assigned Product",
                  "description": "Product assigned to storage location",
                  "unit": "piece",
                  "quantity": 0,
                  "minimumQuantity": 0,
                  "status": "ACTIVE",
                  "storageLocationId": %d
                }
                """.formatted(productSku, storageLocationId);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(productSku))
                .andExpect(jsonPath("$.storageLocationId").value(storageLocationId))
                .andExpect(jsonPath("$.storageLocationCode").value(locationCode));

        mockMvc.perform(delete(ApiPaths.STORAGE_LOCATIONS + "/{id}", storageLocationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        Long locationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM storage_locations
                WHERE id = ?
                """, Long.class, storageLocationId);

        Long assignedProductCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM products
                WHERE storage_location_id = ?
                """, Long.class, storageLocationId);

        assertNotNull(locationCount);
        assertNotNull(assignedProductCount);
        assertEquals(1L, locationCount);
        assertEquals(1L, assignedProductCount);
    }
}