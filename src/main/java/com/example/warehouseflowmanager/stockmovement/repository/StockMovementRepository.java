package com.example.warehouseflowmanager.stockmovement.repository;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByMovementAtDesc(Long productId);
}