package com.example.warehouseflowmanager.common.api;

public final class ApiPaths {

    public static final String API_V1 = "/api/v1";

    public static final String HEALTH = API_V1 + "/health";
    public static final String PRODUCTS = API_V1 + "/products";
    public static final String STOCK_MOVEMENTS = API_V1 + "/stock-movements";
    public static final String STORAGE_LOCATIONS = API_V1 + "/storage-locations";

    private ApiPaths() {
    }
}