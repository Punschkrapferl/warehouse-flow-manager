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
                        "Product with id " + request.getProductId() + " not found"
                ));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new InvalidStockMovementException(
                    "Stock movements are only allowed for ACTIVE products"
            );
        }

        int currentQuantity = product.getQuantity();
        int movementQuantity = request.getQuantity();
        int newQuantity;

        if (request.getMovementType() == StockMovementType.INBOUND) {
            newQuantity = currentQuantity + movementQuantity;
        } else if (request.getMovementType() == StockMovementType.OUTBOUND) {
            if (movementQuantity > currentQuantity) {
                throw new InvalidStockMovementException(
                        "Outbound quantity cannot be greater than current stock"
                );
            }
            newQuantity = currentQuantity - movementQuantity;
        } else {
            throw new InvalidStockMovementException("Unsupported stock movement type");
        }

        product.setQuantity(newQuantity);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setMovementType(request.getMovementType());
        stockMovement.setQuantity(movementQuantity);
        stockMovement.setResultingQuantity(newQuantity);
        stockMovement.setNote(request.getNote());
        stockMovement.setMovementAt(Instant.now());

        StockMovement savedMovement = stockMovementRepository.save(stockMovement);
        productRepository.save(product);

        return mapToResponse(savedMovement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(Long productId) {
        List<StockMovement> stockMovements;

        if (productId != null) {
            if (!productRepository.existsById(productId)) {
                throw new ResourceNotFoundException("Product with id " + productId + " not found");
            }
            stockMovements = stockMovementRepository.findByProductIdOrderByMovementAtDesc(productId);
        } else {
            stockMovements = stockMovementRepository.findAllByOrderByMovementAtDesc();
        }

        return stockMovements.stream()
                .map(this::mapToResponse)
                .toList();
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