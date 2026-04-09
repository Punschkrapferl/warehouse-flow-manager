package com.example.warehouseflowmanager.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/*
 * Product entity.
 *
 * Purpose:
 * Represents a product that can exist in the warehouse system.
 *
 * For now, this is only a domain model / database model.
 * It does not yet have a controller, service, or repository.
 *
 * Later, products will be used for:
 * - inventory tracking
 * - warehouse orders
 * - picking tasks
 */
@Entity
public class Product {

    /*
     * Primary key of the product table.
     *
     * @Id marks this field as the unique identifier.
     * @GeneratedValue means the database will generate the id automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * SKU = Stock Keeping Unit.
     * A unique business identifier for the product.
     *
     * nullable = false means this field is required.
     * unique = true means no two products should have the same SKU.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 30)
    private String unit;

    /*
     * Default constructor required by JPA.
     * JPA uses it internally when loading objects from the database.
     */
    public Product() {
    }

    /*
     * Constructor for creating new Product objects in code.
     * id not included here because it is generated automatically.
     */
    public Product(String sku, String name, String description, String unit) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}