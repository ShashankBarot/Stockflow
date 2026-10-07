package com.inventory.management.service;

import com.inventory.management.dto.request.WarehouseRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {

    default ApiResponse<?> getAllWarehouses(Pageable pageable) {
        return getAllWarehouses(null, pageable);
    }

    ApiResponse<?> getAllWarehouses(String search, Pageable pageable);

    ApiResponse<?> getWarehouseById(Long id);

    ApiResponse<?> createWarehouse(WarehouseRequest request);

    ApiResponse<?> updateWarehouse(Long id, WarehouseRequest request);

    ApiResponse<?> deleteWarehouse(Long id);
}

