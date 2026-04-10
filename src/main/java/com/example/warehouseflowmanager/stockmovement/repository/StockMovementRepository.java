package com.example.warehouseflowmanager.stockmovement.repository;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}