package com.inventory.management.service;

import com.inventory.management.dto.request.BrandRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.BrandResponse;
import com.inventory.management.entity.Brand;
import com.inventory.management.repository.BrandRepository;
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
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand brand;

    @BeforeEach
    void setUp() {
        brand = Brand.builder()
                .id(1L)
                .name("Samsung")
                .description("Samsung electronics")
                .build();
    }

    @Test
    void getAllBrands() {
        when(brandRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(brand)));

        ApiResponse<?> response = brandService.getAllBrands(PageRequest.of(0, 10));
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isInstanceOf(Page.class);
    }

    @Test
    void getBrandById() {
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        ApiResponse<?> response = brandService.getBrandById(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((BrandResponse) response.getData()).name()).isEqualTo("Samsung");
    }

    @Test
    void createBrand() {
        when(brandRepository.existsByName("Samsung")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        BrandRequest request = new BrandRequest("Samsung", "Samsung electronics");
        ApiResponse<?> response = brandService.createBrand(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((BrandResponse) response.getData()).name()).isEqualTo("Samsung");
    }
}

