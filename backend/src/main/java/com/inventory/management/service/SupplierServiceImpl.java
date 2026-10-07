package com.inventory.management.service;

import com.inventory.management.dto.request.SupplierRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.SupplierResponse;
import com.inventory.management.entity.Supplier;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository suppliers;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllSuppliers(Pageable pageable) {
        return ApiResponse.ok(suppliers.findAll(pageable).map(SupplierResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getSupplierById(Long id) {
        Supplier supplier = suppliers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return ApiResponse.ok(SupplierResponse.from(supplier));
    }

    @Override
    @Transactional
    public ApiResponse<?> createSupplier(SupplierRequest request) {
        if (suppliers.existsByName(request.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + request.getName() + "' already exists");
        }
        Supplier supplier = suppliers.save(Supplier.builder()
                .name(request.getName().trim())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .build());
        return ApiResponse.ok("Supplier created", SupplierResponse.from(supplier));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = suppliers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        if (!supplier.getName().equals(request.getName()) && suppliers.existsByName(request.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + request.getName() + "' already exists");
        }
        supplier.setName(request.getName().trim());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setContactPhone(request.getContactPhone());
        supplier.setAddress(request.getAddress());
        return ApiResponse.ok("Supplier updated", SupplierResponse.from(suppliers.save(supplier)));
    }
}

