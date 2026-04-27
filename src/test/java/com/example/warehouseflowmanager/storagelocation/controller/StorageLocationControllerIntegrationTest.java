package com.example.warehouseflowmanager.storagelocation.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void shouldCreateStorageLocation() throws Exception {
        String locationCode = generateLocationCode();

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
                .andExpect(jsonPath("$.description").value("Integration test storage location"))
                .andExpect(jsonPath("$.active").value(true));

        Long locationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM storage_locations
                WHERE code = ?
                """, Long.class, locationCode);

        assertNotNull(locationCount);
        assertEquals(1L, locationCount);
    }

    @Test
    void shouldRejectDuplicateStorageLocationCode() throws Exception {
        String locationCode = generateLocationCode();

        createStorageLocationAndReturnId(locationCode, true);

        String duplicateRequest = """
                {
                  "code": "%s",
                  "zone": "ZONE-DUPLICATE",
                  "description": "Duplicate storage location",
                  "active": true
                }
                """.formatted(locationCode);

        mockMvc.perform(post(ApiPaths.STORAGE_LOCATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Storage location with code '" + locationCode + "' already exists"));

        Long locationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM storage_locations
                WHERE code = ?
                """, Long.class, locationCode);

        assertNotNull(locationCount);
        assertEquals(1L, locationCount);
    }

    @Test
    void shouldReturnNotFoundWhenStorageLocationDoesNotExist() throws Exception {
        long missingStorageLocationId = 999999999L;

        mockMvc.perform(get(ApiPaths.STORAGE_LOCATIONS + "/{id}", missingStorageLocationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Storage location with id " + missingStorageLocationId + " not found"));
    }

    @Test
    void shouldDeleteStorageLocationWhenNoProductsAreAssigned() throws Exception {
        String locationCode = generateLocationCode();

        Long storageLocationId = createStorageLocationAndReturnId(locationCode, true);

        mockMvc.perform(delete(ApiPaths.STORAGE_LOCATIONS + "/{id}", storageLocationId))
                .andExpect(status().isNoContent());

        Long locationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM storage_locations
                WHERE id = ?
                """, Long.class, storageLocationId);

        assertNotNull(locationCount);
        assertEquals(0L, locationCount);
    }

    @Test
    void shouldRejectDeletingStorageLocationWhenProductsAreAssigned() throws Exception {
        String locationCode = generateLocationCode();
        String productSku = TEST_PRODUCT_SKU_PREFIX + UUID.randomUUID();

        Long storageLocationId = createStorageLocationAndReturnId(locationCode, true);

        createProductAssignedToLocation(
                productSku,
                "Assigned Product",
                0,
                0,
                "ACTIVE",
                storageLocationId
        );

        mockMvc.perform(delete(ApiPaths.STORAGE_LOCATIONS + "/{id}", storageLocationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Storage location '" + locationCode
                                + "' cannot be deleted because products are still assigned to it"));

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

    @Test
    void shouldRejectUpdatingStorageLocationToDuplicateCode() throws Exception {
        String firstLocationCode = generateLocationCode();
        String secondLocationCode = generateLocationCode();

        createStorageLocationAndReturnId(firstLocationCode, true);
        Long secondStorageLocationId = createStorageLocationAndReturnId(secondLocationCode, true);

        String updateRequest = """
                {
                  "code": "%s"
                }
                """.formatted(firstLocationCode);

        mockMvc.perform(put(ApiPaths.STORAGE_LOCATIONS + "/{id}", secondStorageLocationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Storage location with code '" + firstLocationCode + "' already exists"));

        String currentCode = jdbcTemplate.queryForObject("""
                SELECT code
                FROM storage_locations
                WHERE id = ?
                """, String.class, secondStorageLocationId);

        assertEquals(secondLocationCode, currentCode);
    }

    @Test
    void shouldReturnStorageLocationStockOverview() throws Exception {
        String locationCode = generateLocationCode();

        Long storageLocationId = createStorageLocationAndReturnId(locationCode, true);

        createProductAssignedToLocation(
                TEST_PRODUCT_SKU_PREFIX + UUID.randomUUID(),
                "Low Stock Product",
                1,
                5,
                "ACTIVE",
                storageLocationId
        );

        createProductAssignedToLocation(
                TEST_PRODUCT_SKU_PREFIX + UUID.randomUUID(),
                "Normal Stock Product",
                10,
                2,
                "ACTIVE",
                storageLocationId
        );

        mockMvc.perform(get(ApiPaths.STORAGE_LOCATIONS + "/{id}/stock-overview", storageLocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storageLocationId))
                .andExpect(jsonPath("$.code").value(locationCode))
                .andExpect(jsonPath("$.zone").value("ZONE-T"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.totalProducts").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(11))
                .andExpect(jsonPath("$.lowStockProductCount").value(1))
                .andExpect(jsonPath("$.products", hasSize(2)));
    }

    @Test
    void shouldRejectInvalidStorageLocationCodeFormat() throws Exception {
        String invalidLocationRequest = """
                {
                  "code": "invalid_code",
                  "zone": "ZONE-T",
                  "description": "Invalid code format",
                  "active": true
                }
                """;

        mockMvc.perform(post(ApiPaths.STORAGE_LOCATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLocationRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.code")
                        .value("Location code must contain only uppercase letters, numbers, and hyphens"));
    }

    private Long createStorageLocationAndReturnId(String locationCode, boolean active) throws Exception {
        String createLocationRequest = """
                {
                  "code": "%s",
                  "zone": "ZONE-T",
                  "description": "Integration test storage location",
                  "active": %s
                }
                """.formatted(locationCode, active);

        mockMvc.perform(post(ApiPaths.STORAGE_LOCATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLocationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(locationCode))
                .andExpect(jsonPath("$.zone").value("ZONE-T"))
                .andExpect(jsonPath("$.active").value(active));

        Long storageLocationId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM storage_locations
                WHERE code = ?
                """, Long.class, locationCode);

        assertNotNull(storageLocationId);
        return storageLocationId;
    }

    private void createProductAssignedToLocation(
            String productSku,
            String productName,
            int quantity,
            int minimumQuantity,
            String status,
            Long storageLocationId
    ) throws Exception {
        String createProductRequest = """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "Product assigned to storage location",
                  "unit": "piece",
                  "quantity": %d,
                  "minimumQuantity": %d,
                  "status": "%s",
                  "storageLocationId": %d
                }
                """.formatted(
                productSku,
                productName,
                quantity,
                minimumQuantity,
                status,
                storageLocationId
        );

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(productSku))
                .andExpect(jsonPath("$.storageLocationId").value(storageLocationId));
    }

    private String generateLocationCode() {
        return TEST_LOCATION_CODE_PREFIX + UUID.randomUUID()
                .toString()
                .toUpperCase(Locale.ROOT);
    }
}