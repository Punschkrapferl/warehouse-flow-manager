package com.example.warehouseflowmanager.product.service;

import com.example.warehouseflowmanager.product.dto.CreateProductRequest;
import com.example.warehouseflowmanager.product.dto.ProductResponse;
import com.example.warehouseflowmanager.product.dto.UpdateProductRequest;
import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with this SKU already exists"
            );
        }

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .unit(request.unit())
                .quantity(request.quantity())
                .locationCode(request.locationCode())
                .build();

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id " + id + " was not found"
                ));

        return mapToResponse(product);
    }

    public ProductResponse updateProductById(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id " + id + " was not found"
                ));

        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with this SKU already exists"
            );
        }

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setUnit(request.unit());
        product.setQuantity(request.quantity());
        product.setLocationCode(request.locationCode());

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id " + id + " was not found"
                ));

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnit(),
                product.getQuantity(),
                product.getLocationCode()
        );
    }
}