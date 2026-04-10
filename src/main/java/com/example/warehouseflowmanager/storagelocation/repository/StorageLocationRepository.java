package com.example.warehouseflowmanager.storagelocation.repository;

import com.example.warehouseflowmanager.storagelocation.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    boolean existsByCode(String code);
}