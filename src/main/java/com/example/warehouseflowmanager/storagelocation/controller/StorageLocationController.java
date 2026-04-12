package com.example.warehouseflowmanager.storagelocation.controller;

import com.example.warehouseflowmanager.storagelocation.dto.CreateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationResponse;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationStockOverviewResponse;
import com.example.warehouseflowmanager.storagelocation.dto.UpdateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.service.StorageLocationService;
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
public class StorageLocationController {

    private final StorageLocationService storageLocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageLocationResponse createStorageLocation(
            @Valid @RequestBody CreateStorageLocationRequest request
    ) {
        return storageLocationService.createStorageLocation(request);
    }

    @GetMapping
    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationService.getAllStorageLocations();
    }

    @GetMapping("/{id}")
    public StorageLocationResponse getStorageLocationById(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            Long id
    ) {
        return storageLocationService.getStorageLocationById(id);
    }

    @GetMapping("/{id}/stock-overview")
    public StorageLocationStockOverviewResponse getStorageLocationStockOverview(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            Long id
    ) {
        return storageLocationService.getStorageLocationStockOverview(id);
    }

    @PutMapping("/{id}")
    public StorageLocationResponse updateStorageLocation(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            Long id,
            @Valid @RequestBody UpdateStorageLocationRequest request
    ) {
        return storageLocationService.updateStorageLocation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStorageLocation(
            @PathVariable
            @Positive(message = "Storage location ID must be greater than 0")
            Long id
    ) {
        storageLocationService.deleteStorageLocation(id);
    }
}