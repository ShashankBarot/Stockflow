package com.inventory.management.service;

import com.inventory.management.dto.request.BrandRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.BrandResponse;
import com.inventory.management.entity.Brand;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllBrands(String search, Pageable pageable) {
        Page<Brand> page = (search != null && !search.isBlank())
                ? brandRepository.findByNameContainingIgnoreCase(search.trim(), pageable)
                : brandRepository.findAll(pageable);
        return ApiResponse.ok(page.map(BrandResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return ApiResponse.ok(BrandResponse.from(brand));
    }

    @Override
    @Transactional
    public ApiResponse<?> createBrand(BrandRequest request) {
        if (brandRepository.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("Brand with name '" + request.getName().trim() + "' already exists");
        }
        Brand brand = brandRepository.save(Brand.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build());
        return ApiResponse.ok("Brand created", BrandResponse.from(brand));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (!brand.getName().equalsIgnoreCase(trimmedName) && brandRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Brand with name '" + trimmedName + "' already exists");
        }

        brand.setName(trimmedName);
        brand.setDescription(request.getDescription());
        return ApiResponse.ok("Brand updated", BrandResponse.from(brandRepository.save(brand)));
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        brandRepository.delete(brand);
        return ApiResponse.ok("Brand deleted", null);
    }
}

