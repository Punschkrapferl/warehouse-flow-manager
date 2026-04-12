package com.example.warehouseflowmanager.stockmovement.service;

import com.example.warehouseflowmanager.common.exception.InvalidStockMovementException;
import com.example.warehouseflowmanager.common.exception.ResourceNotFoundException;
import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import com.example.warehouseflowmanager.stockmovement.dto.CreateStockMovementRequest;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementSummaryResponse;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.repository.StockMovementRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Transactional
    public StockMovementResponse createStockMovement(CreateStockMovementRequest request) {
        // Lock the product row before calculating the new quantity so concurrent stock updates
        // do not overwrite each other and produce inconsistent inventory values.
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()
                ));

        if (product.getStatus() == ProductStatus.BLOCKED) {
            throw new InvalidStockMovementException("Blocked products cannot be moved");
        }

        int currentQuantity = product.getQuantity();
        int requestedQuantity = request.getQuantity();

        validateRequestedQuantity(request.getMovementType(), requestedQuantity);

        int resultingQuantity = switch (request.getMovementType()) {
            case INBOUND -> currentQuantity + requestedQuantity;
            case OUTBOUND -> calculateOutboundQuantity(currentQuantity, requestedQuantity);
            // ADJUSTMENT sets the stock to the counted value instead of adding/removing a delta.
            case ADJUSTMENT -> requestedQuantity;
        };

        product.setQuantity(resultingQuantity);
        productRepository.save(product);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setMovementType(request.getMovementType());
        stockMovement.setQuantity(requestedQuantity);
        stockMovement.setResultingQuantity(resultingQuantity);
        stockMovement.setNote(request.getNote());
        stockMovement.setMovementAt(Instant.now());

        StockMovement savedMovement = stockMovementRepository.save(stockMovement);

        return mapToResponse(savedMovement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(
            Long productId,
            StockMovementType movementType,
            Instant from,
            Instant to
    ) {
        validateDateRange(from, to);

        if (productId != null) {
            productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + productId
                    ));
        }

        List<StockMovement> movements = getBaseMovements(productId, movementType);

        return movements.stream()
                .filter(movement -> matchesDateRange(movement, from, to))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByProductId(
            Long productId,
            StockMovementType movementType,
            Instant from,
            Instant to
    ) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId
                ));

        validateDateRange(from, to);

        List<StockMovement> movements = getBaseMovements(productId, movementType);

        return movements.stream()
                .filter(movement -> matchesDateRange(movement, from, to))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockMovementSummaryResponse getStockMovementSummary(
            Long productId,
            Instant from,
            Instant to
    ) {
        validateDateRange(from, to);

        Product product = null;

        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + productId
                    ));
        }

        List<StockMovement> movements = getBaseMovements(productId, null).stream()
                .filter(movement -> matchesDateRange(movement, from, to))
                .toList();

        return buildSummaryResponse(product, from, to, movements);
    }

    @Transactional(readOnly = true)
    public StockMovementSummaryResponse getStockMovementSummaryByProductId(
            Long productId,
            Instant from,
            Instant to
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId
                ));

        validateDateRange(from, to);

        List<StockMovement> movements = getBaseMovements(productId, null).stream()
                .filter(movement -> matchesDateRange(movement, from, to))
                .toList();

        return buildSummaryResponse(product, from, to, movements);
    }

    private StockMovementSummaryResponse buildSummaryResponse(
            Product product,
            Instant from,
            Instant to,
            List<StockMovement> movements
    ) {
        long inboundMovementCount = movements.stream()
                .filter(movement -> movement.getMovementType() == StockMovementType.INBOUND)
                .count();

        long outboundMovementCount = movements.stream()
                .filter(movement -> movement.getMovementType() == StockMovementType.OUTBOUND)
                .count();

        long adjustmentMovementCount = movements.stream()
                .filter(movement -> movement.getMovementType() == StockMovementType.ADJUSTMENT)
                .count();

        int totalInboundQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == StockMovementType.INBOUND)
                .map(StockMovement::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalOutboundQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == StockMovementType.OUTBOUND)
                .map(StockMovement::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalAdjustmentQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == StockMovementType.ADJUSTMENT)
                .map(StockMovement::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        // Repository methods return newest movements first, so index 0 is the latest movement.
        Instant latestMovementAt = movements.isEmpty() ? null : movements.get(0).getMovementAt();

        return new StockMovementSummaryResponse(
                product != null ? product.getId() : null,
                product != null ? product.getSku() : null,
                from,
                to,
                movements.size(),
                inboundMovementCount,
                outboundMovementCount,
                adjustmentMovementCount,
                totalInboundQuantity,
                totalOutboundQuantity,
                totalAdjustmentQuantity,
                product != null ? product.getQuantity() : null,
                latestMovementAt
        );
    }

    private List<StockMovement> getBaseMovements(Long productId, StockMovementType movementType) {
        if (productId != null && movementType != null) {
            return stockMovementRepository.findByProductIdAndMovementTypeOrderByMovementAtDesc(
                    productId,
                    movementType
            );
        }

        if (productId != null) {
            return stockMovementRepository.findByProductIdOrderByMovementAtDesc(productId);
        }

        if (movementType != null) {
            return stockMovementRepository.findByMovementTypeOrderByMovementAtDesc(movementType);
        }

        return stockMovementRepository.findAllByOrderByMovementAtDesc();
    }

    private boolean matchesDateRange(StockMovement movement, Instant from, Instant to) {
        Instant movementAt = movement.getMovementAt();

        if (from != null && movementAt.isBefore(from)) {
            return false;
        }

        return to == null || !movementAt.isAfter(to);
    }

    private void validateRequestedQuantity(StockMovementType movementType, int quantity) {
        switch (movementType) {
            case INBOUND, OUTBOUND -> {
                if (quantity <= 0) {
                    throw new InvalidStockMovementException(
                            movementType + " quantity must be greater than 0"
                    );
                }
            }
            case ADJUSTMENT -> {
                if (quantity < 0) {
                    throw new InvalidStockMovementException(
                            "ADJUSTMENT quantity must be 0 or greater"
                    );
                }
            }
        }
    }

    private int calculateOutboundQuantity(int currentQuantity, int requestedQuantity) {
        if (requestedQuantity > currentQuantity) {
            throw new InvalidStockMovementException(
                    "Outbound quantity cannot exceed current stock"
            );
        }

        return currentQuantity - requestedQuantity;
    }

    private void validateDateRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidStockMovementException(
                    "'from' must be before or equal to 'to'"
            );
        }
    }

    private StockMovementResponse mapToResponse(StockMovement stockMovement) {
        return new StockMovementResponse(
                stockMovement.getId(),
                stockMovement.getProduct().getId(),
                stockMovement.getProduct().getSku(),
                stockMovement.getMovementType(),
                stockMovement.getQuantity(),
                stockMovement.getResultingQuantity(),
                stockMovement.getNote(),
                stockMovement.getMovementAt()
        );
    }
}