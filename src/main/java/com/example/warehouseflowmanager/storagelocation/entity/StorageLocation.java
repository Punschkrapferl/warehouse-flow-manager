package com.example.warehouseflowmanager.storagelocation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "storage_locations")
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String zone;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getZone() {
        return zone;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}