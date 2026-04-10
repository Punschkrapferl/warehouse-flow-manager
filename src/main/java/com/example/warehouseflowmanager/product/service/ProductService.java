package com.example.warehouseflowmanager.product.service;

import com.example.warehouseflowmanager.common.dto.PagedResponse;
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
import com.example.warehouseflowmanager.stockmovement.repository.StockMovementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku().trim())) {
            throw new ResourceConflictException(
                    "Product with SKU '" + request.getSku().trim() + "' already exists"
            );
        }

        Product product = new Product();
        product.setSku(request.getSku().trim());
        product.setName(request.getName().trim());
        product.setDescription(normalizeDescription(request.getDescription()));
        product.setUnit(request.getUnit().trim());
        product.setQuantity(request.getQuantity());
        product.setMinimumQuantity(normalizeMinimumQuantity(request.getMinimumQuantity()));
        product.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);
        product.setStorageLocation(resolveStorageLocation(request.getStorageLocationId()));

        Product savedProduct = productRepository.save(product);
        Product savedProductWithStorageLocation = getProductWithStorageLocation(savedProduct.getId());

        return mapToResponse(savedProductWithStorageLocation);
    }

    public PagedResponse<ProductResponse> getProducts(
            ProductStatus status,
            Long storageLocationId,
            String search,
            int page,
            int size
    ) {
        String normalizedSearch = normalizeSearch(search);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizePageSize(size);

        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<ProductResponse> productPage = productRepository.findAllWithFilters(
                status,
                storageLocationId,
                normalizedSearch,
                pageRequest
        ).map(this::mapToResponse);

        return PagedResponse.from(productPage);
    }

    public ProductResponse getProductById(Long id) {
        return mapToResponse(getProductWithStorageLocation(id));
    }

    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts(ProductStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = getProductByIdOrThrow(id);

        String trimmedSku = request.getSku().trim();
        if (productRepository.existsBySkuAndIdNot(trimmedSku, id)) {
            throw new ResourceConflictException(
                    "Product with SKU '" + trimmedSku + "' already exists"
            );
        }

        product.setSku(trimmedSku);
        product.setName(request.getName().trim());
        product.setDescription(normalizeDescription(request.getDescription()));
        product.setUnit(request.getUnit().trim());
        product.setQuantity(request.getQuantity());
        product.setMinimumQuantity(normalizeMinimumQuantity(request.getMinimumQuantity()));
        product.setStatus(request.getStatus() != null ? request.getStatus() : product.getStatus());
        product.setStorageLocation(resolveStorageLocation(request.getStorageLocationId()));

        productRepository.save(product);

        return mapToResponse(getProductWithStorageLocation(product.getId()));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductByIdOrThrow(id);

        if (product.getQuantity() != null && product.getQuantity() > 0) {
            throw new ResourceConflictException(
                    "Product '" + product.getSku() + "' cannot be deleted because it still has stock on hand"
            );
        }

        if (stockMovementRepository.existsByProductId(id)) {
            throw new ResourceConflictException(
                    "Product '" + product.getSku()
                            + "' cannot be deleted because stock movement history exists"
            );
        }

        productRepository.delete(product);
    }

    private Product getProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    private Product getProductWithStorageLocation(Long id) {
        return productRepository.findWithStorageLocationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    private StorageLocation resolveStorageLocation(Long storageLocationId) {
        if (storageLocationId == null) {
            return null;
        }

        return storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Storage location with id " + storageLocationId + " not found"
                ));
    }

    private Integer normalizeMinimumQuantity(Integer minimumQuantity) {
        return minimumQuantity != null ? minimumQuantity : 0;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }
        return search.trim();
    }

    private int normalizePageSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private ProductResponse mapToResponse(Product product) {
        StorageLocation storageLocation = product.getStorageLocation();
        int minimumQuantity = product.getMinimumQuantity() != null ? product.getMinimumQuantity() : 0;
        boolean lowStock = product.getStatus() == ProductStatus.ACTIVE
                && product.getQuantity() <= minimumQuantity;

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnit(),
                product.getQuantity(),
                storageLocation != null ? storageLocation.getId() : null,
                storageLocation != null ? storageLocation.getCode() : null,
                product.getStatus(),
                minimumQuantity,
                lowStock
        );
    }
}