package com.inventory.management.service;

import com.inventory.management.dto.request.CategoryRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.CategoryResponse;
import com.inventory.management.entity.Category;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllCategories(String search, Pageable pageable) {
        Page<Category> page = (search != null && !search.isBlank())
                ? categoryRepository.findByNameContainingIgnoreCase(search.trim(), pageable)
                : categoryRepository.findAll(pageable);
        return ApiResponse.ok(page.map(CategoryResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return ApiResponse.ok(CategoryResponse.from(category));
    }

    @Override
    @Transactional
    public ApiResponse<?> createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("Category with name '" + request.getName().trim() + "' already exists");
        }
        Category category = categoryRepository.save(Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build());
        return ApiResponse.ok("Category created", CategoryResponse.from(category));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(trimmedName) && categoryRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
        }

        category.setName(trimmedName);
        category.setDescription(request.getDescription());
        return ApiResponse.ok("Category updated", CategoryResponse.from(categoryRepository.save(category)));
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
        return ApiResponse.ok("Category deleted", null);
    }
}

