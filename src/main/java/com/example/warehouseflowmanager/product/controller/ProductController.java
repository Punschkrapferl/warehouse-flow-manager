package com.example.warehouseflowmanager.product.controller;

import com.example.warehouseflowmanager.common.dto.PagedResponse;
import com.example.warehouseflowmanager.product.dto.CreateProductRequest;
import com.example.warehouseflowmanager.product.dto.ProductResponse;
import com.example.warehouseflowmanager.product.dto.UpdateProductRequest;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/low-stock")
    public List<ProductResponse> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    @GetMapping
    public PagedResponse<ProductResponse> getProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long storageLocationId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return productService.getProducts(
                status,
                storageLocationId,
                search,
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}