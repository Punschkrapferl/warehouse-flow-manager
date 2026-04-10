package com.example.warehouseflowmanager.storagelocation.controller;

import com.example.warehouseflowmanager.storagelocation.dto.CreateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationResponse;
import com.example.warehouseflowmanager.storagelocation.dto.UpdateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.service.StorageLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage-locations")
@RequiredArgsConstructor
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
    public StorageLocationResponse getStorageLocationById(@PathVariable Long id) {
        return storageLocationService.getStorageLocationById(id);
    }

    @PutMapping("/{id}")
    public StorageLocationResponse updateStorageLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStorageLocationRequest request
    ) {
        return storageLocationService.updateStorageLocation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStorageLocation(@PathVariable Long id) {
        storageLocationService.deleteStorageLocation(id);
    }
}