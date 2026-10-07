package com.inventory.management.dto.response;

import com.inventory.management.entity.Inventory;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        ProductResponse product,
        WarehouseResponse warehouse,
        Integer quantity,
        Integer minThreshold,
        Integer maxCapacity,
        LocalDateTime updatedAt
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct() != null ? ProductResponse.from(inventory.getProduct()) : null,
                inventory.getWarehouse() != null ? WarehouseResponse.from(inventory.getWarehouse()) : null,
                inventory.getQuantity(),
                inventory.getMinThreshold(),
                inventory.getMaxCapacity(),
                inventory.getUpdatedAt()
        );
    }
}

