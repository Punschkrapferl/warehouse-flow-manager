package com.example.warehouseflowmanager.storagelocation.controller;

import com.example.warehouseflowmanager.storagelocation.dto.CreateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationResponse;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationStockOverviewResponse;
import com.example.warehouseflowmanager.storagelocation.dto.UpdateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.service.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage-locations")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Storage Locations",
        description = "Manage warehouse storage locations and view stock overview by location"
)
public class StorageLocationController {

    private final StorageLocationService storageLocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create storage location",
            description = "Creates a new storage location"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Storage location created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "A conflicting storage location already exists")
    })
    public StorageLocationResponse createStorageLocation(
            @Valid
            @RequestBody
            @Parameter(description = "Storage location creation payload")
            CreateStorageLocationRequest request
    ) {
        return storageLocationService.createStorageLocation(request);
    }

    @GetMapping
    @Operation(
            summary = "Get all storage locations",
            description = "Returns all storage locations"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Storage locations retrieved successfully")
    })
    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationService.getAllStorageLocations();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get storage location by ID",
            description = "Returns a single storage location by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Storage location retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid storage location ID"),
            @ApiResponse(responseCode = "404", description = "Storage location not found")
    })
    public StorageLocationResponse getStorageLocationById(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            @Parameter(description = "Storage location ID", example = "1")
            Long id
    ) {
        return storageLocationService.getStorageLocationById(id);
    }

    @GetMapping("/{id}/stock-overview")
    @Operation(
            summary = "Get storage location stock overview",
            description = "Returns aggregated stock information and product details for a storage location"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Storage location stock overview retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid storage location ID"),
            @ApiResponse(responseCode = "404", description = "Storage location not found")
    })
    public StorageLocationStockOverviewResponse getStorageLocationStockOverview(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            @Parameter(description = "Storage location ID", example = "1")
            Long id
    ) {
        return storageLocationService.getStorageLocationStockOverview(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update storage location",
            description = "Updates a storage location"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Storage location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Storage location not found"),
            @ApiResponse(responseCode = "409", description = "Update would create a conflict")
    })
    public StorageLocationResponse updateStorageLocation(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            @Parameter(description = "Storage location ID", example = "1")
            Long id,

            @Valid
            @RequestBody
            @Parameter(description = "Storage location update payload")
            UpdateStorageLocationRequest request
    ) {
        return storageLocationService.updateStorageLocation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete storage location",
            description = "Deletes a storage location if deletion rules allow it"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Storage location deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid storage location ID"),
            @ApiResponse(responseCode = "404", description = "Storage location not found"),
            @ApiResponse(responseCode = "409", description = "Storage location cannot be deleted because products are assigned")
    })
    public void deleteStorageLocation(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            @Parameter(description = "Storage location ID", example = "1")
            Long id
    ) {
        storageLocationService.deleteStorageLocation(id);
    }
}