package com.example.warehouseflowmanager.storagelocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "StorageLocationResponse",
        description = "Storage location details returned by the API."
)
public class StorageLocationResponse {

    @Schema(
            description = "Unique identifier of the storage location.",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Human-readable storage location code.",
            example = "A-01-01"
    )
    private String code;

    @Schema(
            description = "Warehouse zone of the storage location.",
            example = "ZONE-A"
    )
    private String zone;

    @Schema(
            description = "Optional description of the storage location.",
            example = "Rack A, aisle 1, level 1"
    )
    private String description;

    @Schema(
            description = "Indicates whether the storage location is active and available for use.",
            example = "true"
    )
    private Boolean active;
}