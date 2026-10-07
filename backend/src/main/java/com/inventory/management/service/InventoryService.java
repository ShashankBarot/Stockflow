package com.inventory.management.service;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

/**
 * InventoryService defines the contract for inventory-level and stock movement operations.
 *
 * <p>Product CRUD operations are handled by {@link ProductService}.
 * Warehouse CRUD operations are handled by {@link WarehouseService}.
 *
 * <p>This interface will be implemented in Phase B as part of the StockService /
 * StockMovementService implementation. Until then, InventoryController and MovementController
 * remain deferred (they use @ConditionalOnBean(InventoryService.class) and will not load
 * until a bean implementing this interface is registered).
 */
public interface InventoryService {

    // Inventory ledger operations
    ApiResponse<?> getInventory(Pageable pageable);

    ApiResponse<?> getInventoryByWarehouse(Long warehouseId);

    ApiResponse<?> updateInventoryThreshold(Long id, Object request);

    // Stock movement operations
    ApiResponse<?> createMovement(StockMovementRequest request, Long userId);

    ApiResponse<?> getAllMovements(Pageable pageable);

    ApiResponse<?> getMovementById(Long id);
}
