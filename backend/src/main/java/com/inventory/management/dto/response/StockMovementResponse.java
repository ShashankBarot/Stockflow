package com.inventory.management.dto.response;

import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        ProductResponse product,
        WarehouseResponse warehouse,
        MovementType type,
        Integer quantity,
        String referenceNumber,
        UserResponse performedBy,
        String notes,
        LocalDateTime createdAt
) {
    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct() != null ? ProductResponse.from(movement.getProduct()) : null,
                movement.getWarehouse() != null ? WarehouseResponse.from(movement.getWarehouse()) : null,
                movement.getType(),
                movement.getQuantity(),
                movement.getReferenceNumber(),
                movement.getPerformedBy() != null ? UserResponse.from(movement.getPerformedBy()) : null,
                movement.getNotes(),
                movement.getCreatedAt()
        );
    }
}

