package com.example.warehouseflowmanager.product.controller;

import com.example.warehouseflowmanager.common.api.ApiErrorResponse;
import com.example.warehouseflowmanager.common.dto.PagedResponse;
import com.example.warehouseflowmanager.product.dto.CreateProductRequest;
import com.example.warehouseflowmanager.product.dto.ProductResponse;
import com.example.warehouseflowmanager.product.dto.ReplenishmentRecommendationResponse;
import com.example.warehouseflowmanager.product.dto.UpdateProductRequest;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import com.example.warehouseflowmanager.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Products",
        description = "Manage warehouse products, filtering, pagination, sorting, low-stock tracking, and replenishment recommendations"
)
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create product",
            description = """
                    Creates a new product.
                    If an initial quantity greater than zero is provided, an initial INBOUND stock movement is created automatically.
                    Blocked products cannot be created with initial stock.
                    Products cannot be assigned to inactive storage locations.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or business rule rejected the request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A conflicting product already exists or the selected storage location is inactive",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ProductResponse createProduct(
            @Valid
            @RequestBody
            @Parameter(description = "Product creation payload")
            CreateProductRequest request
    ) {
        return productService.createProduct(request);
    }

    @GetMapping("/low-stock")
    @Operation(
            summary = "Get low-stock products",
            description = "Returns all active products whose quantity is at or below their minimum quantity threshold"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Low-stock products retrieved successfully")
    })
    public List<ProductResponse> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    @GetMapping("/replenishment-candidates")
    @Operation(
            summary = "Get replenishment candidates",
            description = """
                    Returns active products that currently need replenishment.
                    Recommendations are calculated from current shortage against minimum quantity
                    plus recent outbound demand, then sorted by business priority.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Replenishment candidates retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed for the recentDays parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public List<ReplenishmentRecommendationResponse> getReplenishmentCandidates(
            @RequestParam(defaultValue = "30")
            @Positive(message = "recentDays must be greater than 0")
            @Max(value = 365, message = "recentDays must not be greater than 365")
            @Parameter(description = "Number of recent days used to evaluate outbound demand", example = "30")
            int recentDays
    ) {
        return productService.getReplenishmentCandidates(recentDays);
    }

    @GetMapping
    @Operation(
            summary = "Get products",
            description = """
                    Returns products with optional filtering, pagination, and sorting.
                    Filters can be combined by status, storage location, and free-text search.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed for one or more query parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public PagedResponse<ProductResponse> getProducts(
            @RequestParam(required = false)
            @Parameter(description = "Optional product status filter")
            ProductStatus status,

            @RequestParam(required = false)
            @Positive(message = "Storage location ID must be greater than 0")
            @Parameter(description = "Optional storage location ID filter", example = "1")
            Long storageLocationId,

            @RequestParam(required = false)
            @Size(max = 100, message = "Search must not exceed 100 characters")
            @Parameter(description = "Optional free-text search over product fields", example = "container")
            String search,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Page must be zero or greater")
            @Parameter(description = "Zero-based page index", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Positive(message = "Size must be greater than 0")
            @Max(value = 100, message = "Size must not be greater than 100")
            @Parameter(description = "Page size", example = "10")
            int size,

            @RequestParam(defaultValue = "id")
            @Parameter(description = "Field used for sorting", example = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            @Pattern(regexp = "(?i)asc|desc", message = "Direction must be either 'asc' or 'desc'")
            @Parameter(
                    description = "Sort direction",
                    example = "asc",
                    schema = @Schema(allowableValues = {"asc", "desc"})
            )
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
    @Operation(
            summary = "Get product by ID",
            description = "Returns a single product by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ProductResponse getProductById(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Product ID", example = "1")
            Long id
    ) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = """
                    Updates product metadata such as SKU, name, description, unit, minimum quantity, status, and storage location.
                    Quantity cannot be changed through this endpoint and must be changed through stock movements instead.
                    Products cannot be assigned to inactive storage locations.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or quantity update was attempted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Update would create a conflict or assign the product to an inactive storage location",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ProductResponse updateProduct(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Product ID", example = "1")
            Long id,

            @Valid
            @RequestBody
            @Parameter(description = "Product update payload")
            UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete product",
            description = "Deletes a product if deletion rules allow it"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Product cannot be deleted because of business rules",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public void deleteProduct(
            @PathVariable
            @Positive(message = "Product ID must be greater than 0")
            @Parameter(description = "Product ID", example = "1")
            Long id
    ) {
        productService.deleteProduct(id);
    }
}