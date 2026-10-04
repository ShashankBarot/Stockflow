package com.inventory.management.service;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    // Product operations
    ApiResponse<?> getAllProducts(Pageable pageable);

    ApiResponse<?> getProductById(Long id);

    ApiResponse<?> createProduct(Object request);

    ApiResponse<?> updateProduct(Long id, Object request);

    // Inventory operations
    ApiResponse<?> getInventory(Pageable pageable);

    ApiResponse<?> getInventoryByWarehouse(Long warehouseId);

    ApiResponse<?> updateInventoryThreshold(Long id, Object request);

    // Movement operations
    ApiResponse<?> createMovement(StockMovementRequest request, Long userId);

    ApiResponse<?> getAllMovements(Pageable pageable);

    ApiResponse<?> getMovementById(Long id);
}
