package com.inventory.management.service;

import com.inventory.management.dto.request.SupplierRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.SupplierResponse;
import com.inventory.management.entity.Supplier;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllSuppliers(String search, Pageable pageable) {
        Page<Supplier> page = (search != null && !search.isBlank())
                ? supplierRepository.findByNameContainingIgnoreCase(search.trim(), pageable)
                : supplierRepository.findAll(pageable);
        return ApiResponse.ok(page.map(SupplierResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return ApiResponse.ok(SupplierResponse.from(supplier));
    }

    @Override
    @Transactional
    public ApiResponse<?> createSupplier(SupplierRequest request) {
        Supplier supplier = supplierRepository.save(Supplier.builder()
                .name(request.getName().trim())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());
        return ApiResponse.ok("Supplier created", SupplierResponse.from(supplier));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(request.getName().trim());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        return ApiResponse.ok("Supplier updated", SupplierResponse.from(supplierRepository.save(supplier)));
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplierRepository.delete(supplier);
        return ApiResponse.ok("Supplier deleted", null);
    }
}

