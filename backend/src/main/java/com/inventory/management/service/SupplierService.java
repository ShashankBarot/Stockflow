package com.inventory.management.service;

import com.inventory.management.dto.request.SupplierRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    ApiResponse<?> getAllSuppliers(String search, Pageable pageable);

    ApiResponse<?> getSupplierById(Long id);

    ApiResponse<?> createSupplier(SupplierRequest request);

    ApiResponse<?> updateSupplier(Long id, SupplierRequest request);

    ApiResponse<?> deleteSupplier(Long id);
}

