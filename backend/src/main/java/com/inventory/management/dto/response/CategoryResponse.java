package com.inventory.management.dto.response;

import com.inventory.management.entity.Category;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
        );
    }
}
