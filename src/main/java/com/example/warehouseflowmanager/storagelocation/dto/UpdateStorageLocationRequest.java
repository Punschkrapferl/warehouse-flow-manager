package com.example.warehouseflowmanager.storagelocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request payload for updating a storage location")
public class UpdateStorageLocationRequest {

    @Size(max = 50, message = "Location code must not exceed 50 characters")
    @Pattern(
            regexp = "^[A-Z0-9-]+$",
            message = "Location code must contain only uppercase letters, numbers, and hyphens"
    )
    @Schema(
            description = "Unique storage location code",
            example = "A-01-01",
            nullable = true
    )
    private String code;

    @Size(max = 100, message = "Zone must not exceed 100 characters")
    @Schema(
            description = "Warehouse zone name",
            example = "ZONE-A",
            nullable = true
    )
    private String zone;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(
            description = "Human-readable storage location description",
            example = "Rack A, aisle 1, level 1",
            nullable = true
    )
    private String description;

    @Schema(
            description = "Whether the storage location is active",
            example = "true",
            nullable = true
    )
    private Boolean active;
}