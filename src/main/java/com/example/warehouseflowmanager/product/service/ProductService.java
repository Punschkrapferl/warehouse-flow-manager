package com.example.warehouseflowmanager.product.service;

import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * Service layer for Product-related business logic.
 *
 * Purpose:
 * Keep business logic separate from the web layer and database layer.
 *
 * The controller should handle HTTP requests.
 * The repository should handle database access.
 * The service sits in between and decides what business actions happen.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    /*
     * Constructor injection.
     *
     * Spring automatically provides the ProductRepository bean here.
     * This is the recommended way to inject dependencies.
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /*
     * Returns all products from the persistence layer.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /*
     * Creates and stores a new product.
     *
     * For now, this is still very simple.
     * Later, later business rules can be added here such as:
     * - checking whether the SKU already exists
     * - validating allowed units
     * - normalizing values before saving
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
}