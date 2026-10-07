package com.inventory.management.service;

import com.inventory.management.dto.request.WarehouseRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {

    ApiResponse<?> getAllWarehouses(Pageable pageable);

    ApiResponse<?> getWarehouseById(Long id);

    ApiResponse<?> createWarehouse(WarehouseRequest request);
}

