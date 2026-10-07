package com.inventory.management.service;

import com.inventory.management.dto.request.BrandRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.BrandResponse;
import com.inventory.management.entity.Brand;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brands;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllBrands(Pageable pageable) {
        return ApiResponse.ok(brands.findAll(pageable).map(BrandResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getBrandById(Long id) {
        Brand brand = brands.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return ApiResponse.ok(BrandResponse.from(brand));
    }

    @Override
    @Transactional
    public ApiResponse<?> createBrand(BrandRequest request) {
        if (brands.existsByName(request.getName())) {
            throw new IllegalArgumentException("Brand with name '" + request.getName() + "' already exists");
        }
        Brand brand = brands.save(Brand.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build());
        return ApiResponse.ok("Brand created", BrandResponse.from(brand));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateBrand(Long id, BrandRequest request) {
        Brand brand = brands.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        if (!brand.getName().equals(request.getName()) && brands.existsByName(request.getName())) {
            throw new IllegalArgumentException("Brand with name '" + request.getName() + "' already exists");
        }
        brand.setName(request.getName().trim());
        brand.setDescription(request.getDescription());
        return ApiResponse.ok("Brand updated", BrandResponse.from(brands.save(brand)));
    }
}

