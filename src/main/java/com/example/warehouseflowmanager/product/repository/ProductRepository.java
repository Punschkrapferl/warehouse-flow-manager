package com.example.warehouseflowmanager.product.repository;

import com.example.warehouseflowmanager.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Repository for Product entities.
 *
 * Purpose:
 * Provides database access methods for products.
 *
 * By extending JpaRepository, Spring automatically provides
 * common methods such as save, findById, findAll, and deleteById.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}