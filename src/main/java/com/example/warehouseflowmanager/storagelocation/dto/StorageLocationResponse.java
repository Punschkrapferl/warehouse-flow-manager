package com.example.warehouseflowmanager.storagelocation.dto;

public class StorageLocationResponse {

    private Long id;
    private String code;
    private String zone;
    private String description;
    private Boolean active;

    public StorageLocationResponse() {
    }

    public StorageLocationResponse(
            Long id,
            String code,
            String zone,
            String description,
            Boolean active
    ) {
        this.id = id;
        this.code = code;
        this.zone = zone;
        this.description = description;
        this.active = active;
    }

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
}