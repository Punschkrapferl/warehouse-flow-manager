package com.example.warehouseflowmanager.stockmovement.repository;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByMovementAtDesc(Long productId);

    List<StockMovement> findAllByOrderByMovementAtDesc();

    boolean existsByProductId(Long productId);
}