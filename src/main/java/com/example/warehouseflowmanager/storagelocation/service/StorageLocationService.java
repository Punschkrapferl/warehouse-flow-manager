package com.example.warehouseflowmanager.storagelocation.service;

import com.example.warehouseflowmanager.common.exception.ResourceConflictException;
import com.example.warehouseflowmanager.common.exception.ResourceNotFoundException;
import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import com.example.warehouseflowmanager.storagelocation.dto.CreateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationResponse;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationStockItemResponse;
import com.example.warehouseflowmanager.storagelocation.dto.StorageLocationStockOverviewResponse;
import com.example.warehouseflowmanager.storagelocation.dto.UpdateStorageLocationRequest;
import com.example.warehouseflowmanager.storagelocation.entity.StorageLocation;
import com.example.warehouseflowmanager.storagelocation.repository.StorageLocationRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageLocationService {

    private final StorageLocationRepository storageLocationRepository;
    private final ProductRepository productRepository;

    public StorageLocationResponse createStorageLocation(CreateStorageLocationRequest request) {
        if (storageLocationRepository.existsByCode(request.getCode())) {
            throw new ResourceConflictException(
                    "Storage location with code '" + request.getCode() + "' already exists"
            );
        }

        StorageLocation storageLocation = new StorageLocation();
        storageLocation.setCode(request.getCode());
        storageLocation.setZone(request.getZone());
        storageLocation.setDescription(request.getDescription());
        storageLocation.setActive(request.getActive() != null ? request.getActive() : true);

        StorageLocation savedStorageLocation = storageLocationRepository.save(storageLocation);
        return mapToResponse(savedStorageLocation);
    }

    public List<StorageLocationResponse> getAllStorageLocations() {
        return storageLocationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StorageLocationResponse getStorageLocationById(Long id) {
        StorageLocation storageLocation = findStorageLocationById(id);
        return mapToResponse(storageLocation);
    }

    public StorageLocationStockOverviewResponse getStorageLocationStockOverview(Long id) {
        StorageLocation storageLocation = findStorageLocationById(id);

        List<Product> products = productRepository.findAllByStorageLocationIdWithStorageLocation(id);

        List<StorageLocationStockItemResponse> productItems = products.stream()
                .map(this::mapToStockItemResponse)
                .toList();

        int totalQuantity = products.stream()
                .map(Product::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int lowStockProductCount = (int) products.stream()
                .filter(this::isLowStock)
                .count();

        return new StorageLocationStockOverviewResponse(
                storageLocation.getId(),
                storageLocation.getCode(),
                storageLocation.getZone(),
                storageLocation.getDescription(),
                storageLocation.getActive(),
                productItems.size(),
                totalQuantity,
                lowStockProductCount,
                productItems
        );
    }

    public StorageLocationResponse updateStorageLocation(Long id, UpdateStorageLocationRequest request) {
        StorageLocation storageLocation = findStorageLocationById(id);

        if (request.getCode() != null && !request.getCode().equals(storageLocation.getCode())) {
            if (storageLocationRepository.existsByCode(request.getCode())) {
                throw new ResourceConflictException(
                        "Storage location with code '" + request.getCode() + "' already exists"
                );
            }
            storageLocation.setCode(request.getCode());
        }

        if (request.getZone() != null) {
            storageLocation.setZone(request.getZone());
        }

        if (request.getDescription() != null) {
            storageLocation.setDescription(request.getDescription());
        }

        if (request.getActive() != null) {
            storageLocation.setActive(request.getActive());
        }

        StorageLocation updatedStorageLocation = storageLocationRepository.save(storageLocation);
        return mapToResponse(updatedStorageLocation);
    }

    public void deleteStorageLocation(Long id) {
        StorageLocation storageLocation = findStorageLocationById(id);

        // Prevent deletion while products still reference this location to avoid orphaned assignments
        // and to keep location-based inventory data consistent.
        if (productRepository.existsByStorageLocationId(id)) {
            throw new ResourceConflictException(
                    "Storage location '" + storageLocation.getCode()
                            + "' cannot be deleted because products are still assigned to it"
            );
        }

        storageLocationRepository.delete(storageLocation);
    }

    private StorageLocation findStorageLocationById(Long id) {
        return storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Storage location with id " + id + " not found"
                ));
    }

    private StorageLocationResponse mapToResponse(StorageLocation storageLocation) {
        return new StorageLocationResponse(
                storageLocation.getId(),
                storageLocation.getCode(),
                storageLocation.getZone(),
                storageLocation.getDescription(),
                storageLocation.getActive()
        );
    }

    private StorageLocationStockItemResponse mapToStockItemResponse(Product product) {
        Integer minimumQuantity = product.getMinimumQuantity() != null ? product.getMinimumQuantity() : 0;

        return new StorageLocationStockItemResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getUnit(),
                product.getQuantity(),
                minimumQuantity,
                product.getStatus(),
                isLowStock(product)
        );
    }

    private boolean isLowStock(Product product) {
        int quantity = product.getQuantity() != null ? product.getQuantity() : 0;
        int minimumQuantity = product.getMinimumQuantity() != null ? product.getMinimumQuantity() : 0;

        // Keep low-stock semantics aligned with ProductService / ProductResponse:
        // only ACTIVE products are considered low stock.
        return product.getStatus() == ProductStatus.ACTIVE && quantity <= minimumQuantity;
    }
}