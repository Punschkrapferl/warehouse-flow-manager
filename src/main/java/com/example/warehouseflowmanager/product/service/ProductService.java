package com.example.warehouseflowmanager.product.service;

import com.example.warehouseflowmanager.common.exception.ResourceConflictException;
import com.example.warehouseflowmanager.common.exception.ResourceNotFoundException;
import com.example.warehouseflowmanager.product.dto.CreateProductRequest;
import com.example.warehouseflowmanager.product.dto.ProductResponse;
import com.example.warehouseflowmanager.product.dto.UpdateProductRequest;
import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import com.example.warehouseflowmanager.storagelocation.entity.StorageLocation;
import com.example.warehouseflowmanager.storagelocation.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StorageLocationRepository storageLocationRepository;

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResourceConflictException("Product with SKU '" + request.getSku() + "' already exists");
        }

        StorageLocation storageLocation = findStorageLocationById(request.getStorageLocationId());

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setQuantity(request.getQuantity());
        product.setStorageLocation(storageLocation);
        product.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = findProductById(id);
        return mapToResponse(product);
    }

    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProductById(id);

        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new ResourceConflictException("Product with SKU '" + request.getSku() + "' already exists");
            }
            product.setSku(request.getSku());
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }

        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }

        if (request.getStorageLocationId() != null) {
            StorageLocation storageLocation = findStorageLocationById(request.getStorageLocationId());
            product.setStorageLocation(storageLocation);
        }

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    private StorageLocation findStorageLocationById(Long id) {
        return storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Storage location with id " + id + " not found"
                ));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .unit(product.getUnit())
                .quantity(product.getQuantity())
                .storageLocationId(product.getStorageLocation().getId())
                .storageLocationCode(product.getStorageLocation().getCode())
                .status(product.getStatus())
                .build();
    }
}