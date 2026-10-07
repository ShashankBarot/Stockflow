package com.inventory.management.service;

import com.inventory.management.dto.request.InventoryThresholdRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    ApiResponse<?> getInventory(Long productId, Long warehouseId, Boolean lowStock, Pageable pageable);

    ApiResponse<?> getInventoryByWarehouse(Long warehouseId, Pageable pageable);

    ApiResponse<?> getInventoryById(Long id);

    ApiResponse<?> updateInventoryThreshold(Long id, InventoryThresholdRequest request);
}
