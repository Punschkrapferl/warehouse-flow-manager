package com.example.warehouseflowmanager.storagelocation.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateStorageLocationRequest {

    @Size(max = 50, message = "Location code must not exceed 50 characters")
    @Pattern(
            regexp = "^[A-Z0-9-]+$",
            message = "Location code must contain only uppercase letters, numbers, and hyphens"
    )
    private String code;

    @Size(max = 100, message = "Zone must not exceed 100 characters")
    private String zone;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean active;
}