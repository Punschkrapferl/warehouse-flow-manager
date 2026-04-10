package com.example.warehouseflowmanager.product.repository;

import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @EntityGraph(attributePaths = "storageLocation")
    @Query("select p from Product p order by p.id asc")
    List<Product> findAllWithStorageLocation();

    @EntityGraph(attributePaths = "storageLocation")
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findWithStorageLocationById(@Param("id") Long id);

    @EntityGraph(attributePaths = "storageLocation")
    @Query("""
           select p
           from Product p
           where p.status = :status
             and p.quantity <= coalesce(p.minimumQuantity, 0)
           order by p.quantity asc, p.name asc
           """)
    List<Product> findLowStockProducts(@Param("status") ProductStatus status);
}