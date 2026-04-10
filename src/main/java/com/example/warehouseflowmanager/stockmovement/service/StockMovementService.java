package com.example.warehouseflowmanager.stockmovement.service;

import com.example.warehouseflowmanager.common.exception.InvalidStockMovementException;
import com.example.warehouseflowmanager.common.exception.ResourceNotFoundException;
import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import com.example.warehouseflowmanager.stockmovement.dto.CreateStockMovementRequest;
import com.example.warehouseflowmanager.stockmovement.dto.StockMovementResponse;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import com.example.warehouseflowmanager.stockmovement.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Transactional
    public StockMovementResponse createStockMovement(CreateStockMovementRequest request) {
        Product product = productRepository.findById(request.getProductId())
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
    public List<StockMovementResponse> getStockMovements(Long productId) {
        return getStockMovements(productId, null);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(Long productId, StockMovementType movementType) {
        if (productId != null) {
            return getStockMovementsByProductId(productId, movementType);
        }

        List<StockMovement> movements;

        if (movementType != null) {
            movements = stockMovementRepository.findByMovementTypeOrderByMovementAtDesc(movementType);
        } else {
            movements = stockMovementRepository.findAllByOrderByMovementAtDesc();
        }

        return movements.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByProductId(Long productId) {
        return getStockMovementsByProductId(productId, null);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByProductId(Long productId, StockMovementType movementType) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId
                ));

        List<StockMovement> movements;

        if (movementType != null) {
            movements = stockMovementRepository.findByProductIdAndMovementTypeOrderByMovementAtDesc(
                    productId,
                    movementType
            );
        } else {
            movements = stockMovementRepository.findByProductIdOrderByMovementAtDesc(productId);
        }

        return movements.stream()
                .map(this::mapToResponse)
                .toList();
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