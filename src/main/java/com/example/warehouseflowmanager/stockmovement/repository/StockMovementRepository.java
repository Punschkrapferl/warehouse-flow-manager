package com.example.warehouseflowmanager.stockmovement.repository;

import com.example.warehouseflowmanager.stockmovement.entity.StockMovement;
import com.example.warehouseflowmanager.stockmovement.entity.StockMovementType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByMovementAtDesc(Long productId);

    List<StockMovement> findByProductIdAndMovementTypeOrderByMovementAtDesc(
            Long productId,
            StockMovementType movementType
    );

    List<StockMovement> findAllByOrderByMovementAtDesc();

    List<StockMovement> findByMovementTypeOrderByMovementAtDesc(StockMovementType movementType);

    boolean existsByProductId(Long productId);

    @Query("""
           select sm.product.id as productId,
                  coalesce(sum(sm.quantity), 0) as totalQuantity
           from StockMovement sm
           where sm.product.id in :productIds
             and sm.movementType = :movementType
             and sm.movementAt >= :from
           group by sm.product.id
           """)
    List<ProductQuantityTotal> sumQuantityByProductIdsAndTypeSince(
            @Param("productIds") List<Long> productIds,
            @Param("movementType") StockMovementType movementType,
            @Param("from") Instant from
    );

    interface ProductQuantityTotal {
        Long getProductId();
        Long getTotalQuantity();
    }
}