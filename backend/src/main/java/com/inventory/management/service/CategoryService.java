package com.inventory.management.service;

import com.inventory.management.dto.request.CategoryRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    ApiResponse<?> getAllCategories(Pageable pageable);
    ApiResponse<?> getCategoryById(Long id);
    ApiResponse<?> createCategory(CategoryRequest request);
    ApiResponse<?> updateCategory(Long id, CategoryRequest request);
}

