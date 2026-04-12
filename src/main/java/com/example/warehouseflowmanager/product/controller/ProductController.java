package com.example.warehouseflowmanager.product.controller;

import com.example.warehouseflowmanager.common.dto.PagedResponse;
import com.example.warehouseflowmanager.product.dto.CreateProductRequest;
import com.example.warehouseflowmanager.product.dto.ProductResponse;
import com.example.warehouseflowmanager.product.dto.UpdateProductRequest;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@Validated
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
            @RequestParam(required = false)
            @Positive(message = "Storage location ID must be greater than 0")
            Long storageLocationId,
            @RequestParam(required = false)
            @Size(max = 100, message = "Search must not exceed 100 characters")
            String search,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Page must be zero or greater")
            int page,
            @RequestParam(defaultValue = "10")
            @Positive(message = "Size must be greater than 0")
            @Max(value = 100, message = "Size must not be greater than 100")
            int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc")
            @Pattern(regexp = "(?i)asc|desc", message = "Direction must be either 'asc' or 'desc'")
            String direction
    ) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();

        return productService.getProducts(
                status,
                storageLocationId,
                normalizedSearch,
                page,
                size,
                sortBy,
                direction.toLowerCase(Locale.ROOT)
        );
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            Long id
    ) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            Long id
    ) {
        productService.deleteProduct(id);
    }
}