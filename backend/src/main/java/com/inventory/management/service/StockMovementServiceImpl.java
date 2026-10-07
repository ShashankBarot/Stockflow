package com.inventory.management.service;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.StockMovementResponse;
import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final MovementRepository movementRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public ApiResponse<?> createMovement(StockMovementRequest request, Long userId) {
        if (request == null) {
            throw new IllegalArgumentException("Stock movement request cannot be null");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Movement type is required");
        }

        StockMovement movement;
        switch (request.getType()) {
            case INBOUND -> movement = stockService.receiveStock(
                    request.getProductId(),
                    request.getWarehouseId(),
                    request.getQuantity(),
                    request.getReferenceNumber(),
                    userId,
                    request.getNotes()
            );
            case OUTBOUND -> movement = stockService.deductStock(
                    request.getProductId(),
                    request.getWarehouseId(),
                    request.getQuantity(),
                    request.getReferenceNumber(),
                    userId,
                    request.getNotes()
            );
            case ADJUSTMENT -> movement = stockService.adjustStock(
                    request.getProductId(),
                    request.getWarehouseId(),
                    request.getQuantity(),
                    request.getReferenceNumber(),
                    userId,
                    request.getNotes()
            );
            default -> throw new IllegalArgumentException("Unsupported movement type: " + request.getType());
        }

        return ApiResponse.ok("Stock movement created", StockMovementResponse.from(movement));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllMovements(MovementType type, Long productId, Long warehouseId, Pageable pageable) {
        Page<StockMovement> page;

        if (type != null) {
            page = movementRepository.findByType(type, pageable);
        } else if (productId != null) {
            page = movementRepository.findByProductId(productId, pageable);
        } else if (warehouseId != null) {
            page = movementRepository.findByWarehouseId(warehouseId, pageable);
        } else {
            page = movementRepository.findAll(pageable);
        }

        return ApiResponse.ok(page.map(StockMovementResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getMovementById(Long id) {
        StockMovement movement = movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
        return ApiResponse.ok(StockMovementResponse.from(movement));
    }
}

