package com.inventory.management.dto.response;

import com.inventory.management.entity.Supplier;

import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String contactEmail,
        String contactPhone,
        String address,
        LocalDateTime createdAt
) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactEmail(),
                supplier.getContactPhone(),
                supplier.getAddress(),
                supplier.getCreatedAt()
        );
    }
}

