package com.inventory.management.dto.response;

import com.inventory.management.entity.Warehouse;

import java.time.LocalDateTime;

public record WarehouseResponse(
        Long id,
        String name,
        String location,
        LocalDateTime createdAt
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCreatedAt()
        );
    }
}

