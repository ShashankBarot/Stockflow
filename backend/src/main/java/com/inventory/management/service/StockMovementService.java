package com.inventory.management.service;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.entity.StockMovement.MovementType;
import org.springframework.data.domain.Pageable;

public interface StockMovementService {

    ApiResponse<?> createMovement(StockMovementRequest request, Long userId);

    ApiResponse<?> getAllMovements(MovementType type, Long productId, Long warehouseId, Pageable pageable);

    ApiResponse<?> getMovementById(Long id);
}

