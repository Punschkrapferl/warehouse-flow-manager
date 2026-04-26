package com.example.warehouseflowmanager.product.service;

import com.example.warehouseflowmanager.common.dto.PagedResponse;
import com.example.warehouseflowmanager.common.exception.InvalidRequestException;
import com.example.warehouseflowmanager.common.exception.InvalidStockMovementException;
import com.example.warehouseflowmanager.common.exception.ResourceConflictException;
import com.example.warehouseflowmanager.common.exception.ResourceNotFoundException;
import com.example.warehouseflowmanager.product.dto.CreateProductRequest;
import com.example.warehouseflowmanager.product.dto.ProductResponse;
import com.example.warehouseflowmanager.product.dto.ReplenishmentRecommendationResponse;
import com.example.warehouseflowmanager.product.dto.UpdateProductRequest;
import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.repository.StockMovementRepository;
import com.example.warehouseflowmanager.storagelocation.entity.StorageLocation;
import com.example.warehouseflowmanager.storagelocation.repository.StorageLocationRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "sku",
            "name",
            "quantity",
            "status",
            "minimumQuantity"
    );

    private static final String PRIORITY_CRITICAL = "CRITICAL";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String PRIORITY_MEDIUM = "MEDIUM";

    private final ProductRepository productRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String trimmedSku = request.getSku().trim();

        if (productRepository.existsBySku(trimmedSku)) {
            throw new ResourceConflictException(
                    "Product with SKU '" + trimmedSku + "' already exists"
            );
        }

        ProductStatus productStatus = request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE;

        validateInitialQuantityForStatus(productStatus, request.getQuantity());

        Product product = new Product();
        product.setSku(trimmedSku);
        product.setName(request.getName().trim());
        product.setDescription(normalizeDescription(request.getDescription()));
        product.setUnit(request.getUnit().trim());
        product.setQuantity(request.getQuantity());
        product.setMinimumQuantity(normalizeMinimumQuantity(request.getMinimumQuantity()));
        product.setStatus(productStatus);
        product.setStorageLocation(resolveStorageLocationForCreate(request.getStorageLocationId()));

        Product savedProduct = productRepository.save(product);

        createInitialStockMovementIfNeeded(savedProduct);

        Product savedProductWithStorageLocation = getProductWithStorageLocation(savedProduct.getId());
        return mapToResponse(savedProductWithStorageLocation);
    }

    public PagedResponse<ProductResponse> getProducts(
            ProductStatus status,
            Long storageLocationId,
            String search,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        String normalizedSearch = normalizeSearch(search);
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction sortDirection = normalizeSortDirection(direction);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, normalizedSortBy)
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

    public List<ReplenishmentRecommendationResponse> getReplenishmentCandidates(int recentDays) {
        List<Product> candidates = productRepository.findReplenishmentCandidates(ProductStatus.ACTIVE);

        if (candidates.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = candidates.stream()
                .map(Product::getId)
                .toList();

        Instant cutoff = Instant.now().minus(Duration.ofDays(recentDays));

        Map<Long, Integer> recentOutboundByProductId = stockMovementRepository
                .sumQuantityByProductIdsAndTypeSince(
                        productIds,
                        StockMovementType.OUTBOUND,
                        cutoff
                )
                .stream()
                .collect(Collectors.toMap(
                        StockMovementRepository.ProductQuantityTotal::getProductId,
                        projection -> projection.getTotalQuantity() != null
                                ? projection.getTotalQuantity().intValue()
                                : 0
                ));

        return candidates.stream()
                .map(product -> mapToReplenishmentRecommendation(
                        product,
                        recentOutboundByProductId.getOrDefault(product.getId(), 0)
                ))
                .filter(this::shouldIncludeRecommendation)
                .sorted(
                        Comparator
                                .comparingInt((ReplenishmentRecommendationResponse response) ->
                                        getPriorityRank(response.priority()))
                                .thenComparing(
                                        Comparator.comparingInt(
                                                (ReplenishmentRecommendationResponse response) ->
                                                        getSafeInteger(response.recommendedReorderQuantity())
                                        ).reversed()
                                )
                                .thenComparing(
                                        Comparator.comparingInt(
                                                (ReplenishmentRecommendationResponse response) ->
                                                        getSafeInteger(response.recentOutboundQuantity())
                                        ).reversed()
                                )
                                .thenComparing(response ->
                                        response.name() != null ? response.name().toLowerCase() : "")
                                .thenComparing(response ->
                                        response.productId() != null ? response.productId() : Long.MAX_VALUE)
                )
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
        product.setMinimumQuantity(normalizeMinimumQuantity(request.getMinimumQuantity()));
        product.setStatus(request.getStatus() != null ? request.getStatus() : product.getStatus());
        product.setStorageLocation(resolveStorageLocationForUpdate(product, request.getStorageLocationId()));

        productRepository.save(product);

        return mapToResponse(getProductWithStorageLocation(product.getId()));
    }

    @Transactional
    public ProductResponse relocateProduct(Long productId, Long targetStorageLocationId) {
        Product product = getProductByIdOrThrow(productId);
        StorageLocation targetStorageLocation = getStorageLocationByIdOrThrow(targetStorageLocationId);

        if (isSameStorageLocation(product.getStorageLocation(), targetStorageLocation)) {
            throw new ResourceConflictException(
                    "Product with id " + productId
                            + " is already assigned to storage location with id " + targetStorageLocationId
            );
        }

        validateStorageLocationIsActive(targetStorageLocation);

        product.setStorageLocation(targetStorageLocation);
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

    private void validateInitialQuantityForStatus(ProductStatus status, Integer quantity) {
        if (status == ProductStatus.BLOCKED && quantity != null && quantity > 0) {
            throw new InvalidStockMovementException(
                    "Blocked products cannot be created with initial stock"
            );
        }
    }

    private void createInitialStockMovementIfNeeded(Product product) {
        if (product.getQuantity() == null || product.getQuantity() <= 0) {
            return;
        }

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setMovementType(StockMovementType.INBOUND);
        stockMovement.setQuantity(product.getQuantity());
        stockMovement.setResultingQuantity(product.getQuantity());
        stockMovement.setNote("Initial stock on product creation");
        stockMovement.setMovementAt(Instant.now());

        stockMovementRepository.save(stockMovement);
    }

    private Product getProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    private Product getProductWithStorageLocation(Long id) {
        return productRepository.findWithStorageLocationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    private StorageLocation resolveStorageLocationForCreate(Long storageLocationId) {
        if (storageLocationId == null) {
            return null;
        }

        StorageLocation storageLocation = getStorageLocationByIdOrThrow(storageLocationId);
        validateStorageLocationIsActive(storageLocation);
        return storageLocation;
    }

    private StorageLocation resolveStorageLocationForUpdate(Product product, Long storageLocationId) {
        if (storageLocationId == null) {
            return null;
        }

        StorageLocation targetStorageLocation = getStorageLocationByIdOrThrow(storageLocationId);

        if (isSameStorageLocation(product.getStorageLocation(), targetStorageLocation)) {
            return targetStorageLocation;
        }

        validateStorageLocationIsActive(targetStorageLocation);
        return targetStorageLocation;
    }

    private StorageLocation getStorageLocationByIdOrThrow(Long storageLocationId) {
        return storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Storage location with id " + storageLocationId + " not found"
                ));
    }

    private void validateStorageLocationIsActive(StorageLocation storageLocation) {
        if (!Boolean.TRUE.equals(storageLocation.getActive())) {
            throw new ResourceConflictException(
                    "Storage location with id " + storageLocation.getId()
                            + " is inactive and cannot be assigned to a product"
            );
        }
    }

    private boolean isSameStorageLocation(StorageLocation currentStorageLocation, StorageLocation targetStorageLocation) {
        if (currentStorageLocation == null || currentStorageLocation.getId() == null) {
            return false;
        }

        return currentStorageLocation.getId().equals(targetStorageLocation.getId());
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

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }

        String trimmedSortBy = sortBy.trim();

        if (!ALLOWED_SORT_FIELDS.contains(trimmedSortBy)) {
            throw new InvalidRequestException(
                    "Unsupported sort field '" + trimmedSortBy
                            + "'. Allowed values: " + String.join(", ", ALLOWED_SORT_FIELDS)
            );
        }

        return trimmedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }

        try {
            return Sort.Direction.fromString(direction.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException(
                    "Unsupported sort direction '" + direction + "'. Allowed values: asc, desc"
            );
        }
    }

    private ProductResponse mapToResponse(Product product) {
        StorageLocation storageLocation = product.getStorageLocation();
        int currentQuantity = getSafeInteger(product.getQuantity());
        int minimumQuantity = normalizeMinimumQuantity(product.getMinimumQuantity());

        boolean lowStock = product.getStatus() == ProductStatus.ACTIVE
                && currentQuantity <= minimumQuantity;

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnit(),
                currentQuantity,
                storageLocation != null ? storageLocation.getId() : null,
                storageLocation != null ? storageLocation.getCode() : null,
                product.getStatus(),
                minimumQuantity,
                lowStock
        );
    }

    private ReplenishmentRecommendationResponse mapToReplenishmentRecommendation(
            Product product,
            int recentOutboundQuantity
    ) {
        StorageLocation storageLocation = product.getStorageLocation();

        int currentQuantity = getSafeInteger(product.getQuantity());
        int minimumQuantity = normalizeMinimumQuantity(product.getMinimumQuantity());
        int safeRecentOutboundQuantity = Math.max(recentOutboundQuantity, 0);

        int shortageQuantity = Math.max(minimumQuantity - currentQuantity, 0);
        int recommendedReorderQuantity = shortageQuantity + safeRecentOutboundQuantity;

        return new ReplenishmentRecommendationResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getUnit(),
                currentQuantity,
                minimumQuantity,
                shortageQuantity,
                safeRecentOutboundQuantity,
                recommendedReorderQuantity,
                determineReplenishmentPriority(currentQuantity, minimumQuantity, safeRecentOutboundQuantity),
                storageLocation != null ? storageLocation.getId() : null,
                storageLocation != null ? storageLocation.getCode() : null
        );
    }

    private String determineReplenishmentPriority(
            int currentQuantity,
            int minimumQuantity,
            int recentOutboundQuantity
    ) {
        if (currentQuantity <= 0) {
            return PRIORITY_CRITICAL;
        }

        if (currentQuantity < minimumQuantity) {
            return PRIORITY_HIGH;
        }

        int highDemandThreshold = Math.max(1, minimumQuantity / 2);
        if (recentOutboundQuantity >= highDemandThreshold) {
            return PRIORITY_HIGH;
        }

        return PRIORITY_MEDIUM;
    }

    private boolean shouldIncludeRecommendation(ReplenishmentRecommendationResponse recommendation) {
        return getSafeInteger(recommendation.recommendedReorderQuantity()) > 0;
    }

    private int getPriorityRank(String priority) {
        return switch (priority) {
            case PRIORITY_CRITICAL -> 1;
            case PRIORITY_HIGH -> 2;
            case PRIORITY_MEDIUM -> 3;
            default -> 99;
        };
    }

    private int getSafeInteger(Integer value) {
        return value != null ? value : 0;
    }
}