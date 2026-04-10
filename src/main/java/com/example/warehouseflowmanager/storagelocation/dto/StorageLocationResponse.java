package com.example.warehouseflowmanager.storagelocation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StorageLocationResponse {

    private Long id;
    private String code;
    private String zone;
    private String description;
    private Boolean active;
}