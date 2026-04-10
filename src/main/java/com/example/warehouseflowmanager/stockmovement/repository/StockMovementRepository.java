package com.example.warehouseflowmanager.stockmovement.repository;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByMovementAtDesc(Long productId);

    List<StockMovement> findByProductIdAndMovementTypeOrderByMovementAtDesc(
            Long productId,
            StockMovementType movementType
    );

    List<StockMovement> findAllByOrderByMovementAtDesc();

    List<StockMovement> findByMovementTypeOrderByMovementAtDesc(StockMovementType movementType);

    boolean existsByProductId(Long productId);
}