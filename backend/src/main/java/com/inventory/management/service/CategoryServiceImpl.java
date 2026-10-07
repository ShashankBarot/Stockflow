package com.inventory.management.service;

import com.inventory.management.dto.request.CategoryRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.CategoryResponse;
import com.inventory.management.entity.Category;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categories;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllCategories(Pageable pageable) {
        return ApiResponse.ok(categories.findAll(pageable).map(CategoryResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCategoryById(Long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return ApiResponse.ok(CategoryResponse.from(category));
    }

    @Override
    @Transactional
    public ApiResponse<?> createCategory(CategoryRequest request) {
        if (categories.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }
        Category category = categories.save(Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build());
        return ApiResponse.ok("Category created", CategoryResponse.from(category));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateCategory(Long id, CategoryRequest request) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (!category.getName().equals(request.getName()) && categories.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        return ApiResponse.ok("Category updated", CategoryResponse.from(categories.save(category)));
    }
}

