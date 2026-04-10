package com.example.warehouseflowmanager.product.repository;

import com.example.warehouseflowmanager.product.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @EntityGraph(attributePaths = "storageLocation")
    Optional<Product> findWithStorageLocationById(Long id);

    @EntityGraph(attributePaths = "storageLocation")
    List<Product> findAll();
}