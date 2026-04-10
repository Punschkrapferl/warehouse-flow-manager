package com.example.warehouseflowmanager.stockmovement.entity;

import com.example.warehouseflowmanager.product.entity.Product;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer resultingQuantity;

    @Column(length = 255)
    private String note;

    @Column(nullable = false)
    private Instant movementAt;

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(StockMovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getResultingQuantity() {
        return resultingQuantity;
    }

    public void setResultingQuantity(Integer resultingQuantity) {
        this.resultingQuantity = resultingQuantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getMovementAt() {
        return movementAt;
    }

    public void setMovementAt(Instant movementAt) {
        this.movementAt = movementAt;
    }
}