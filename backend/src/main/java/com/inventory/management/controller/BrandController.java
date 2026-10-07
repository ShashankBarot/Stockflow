package com.inventory.management.controller;

import com.inventory.management.dto.request.BrandRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBrandById(
            @PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> createBrand(
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }
}
