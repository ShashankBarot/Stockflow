package com.inventory.management.dto.response;

import com.inventory.management.entity.Brand;

import java.time.LocalDateTime;

public record BrandResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getCreatedAt()
        );
    }
}
