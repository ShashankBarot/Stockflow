package com.inventory.management.service;

import com.inventory.management.dto.request.BrandRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface BrandService {

    ApiResponse<?> getAllBrands(String search, Pageable pageable);

    ApiResponse<?> getBrandById(Long id);

    ApiResponse<?> createBrand(BrandRequest request);

    ApiResponse<?> updateBrand(Long id, BrandRequest request);

    ApiResponse<?> deleteBrand(Long id);
}

