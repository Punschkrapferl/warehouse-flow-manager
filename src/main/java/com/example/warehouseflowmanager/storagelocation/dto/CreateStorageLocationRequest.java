package com.example.warehouseflowmanager.storagelocation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateStorageLocationRequest {

    @NotBlank(message = "Location code must not be blank")
    @Size(max = 50, message = "Location code must not exceed 50 characters")
    @Pattern(
            regexp = "^[A-Z0-9-]+$",
            message = "Location code must contain only uppercase letters, numbers, and hyphens"
    )
    private String code;

    @NotBlank(message = "Zone must not be blank")
    @Size(max = 100, message = "Zone must not exceed 100 characters")
    private String zone;

    @NotBlank(message = "Description must not be blank")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean active;

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