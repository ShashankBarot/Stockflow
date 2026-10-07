package com.inventory.management.service;

import com.inventory.management.dto.request.CategoryRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.CategoryResponse;
import com.inventory.management.entity.Category;
import com.inventory.management.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .build();
    }

    @Test
    void getAllCategories() {
        when(categoryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(category)));

        ApiResponse<?> response = categoryService.getAllCategories(PageRequest.of(0, 10));
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isInstanceOf(Page.class);
    }

    @Test
    void getCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ApiResponse<?> response = categoryService.getCategoryById(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((CategoryResponse) response.getData()).name()).isEqualTo("Electronics");
    }

    @Test
    void createCategory() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryRequest request = new CategoryRequest("Electronics", "Electronic devices");
        ApiResponse<?> response = categoryService.createCategory(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((CategoryResponse) response.getData()).name()).isEqualTo("Electronics");
    }
}
