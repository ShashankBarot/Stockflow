package com.inventory.management.service;

import com.inventory.management.dto.request.WarehouseRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.WarehouseResponse;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouses;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllWarehouses(String search, Pageable pageable) {
        org.springframework.data.domain.Page<Warehouse> page = (search != null && !search.isBlank())
                ? warehouses.findByNameContainingIgnoreCase(search.trim(), pageable)
                : warehouses.findAll(pageable);
        return ApiResponse.ok(page.map(WarehouseResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getWarehouseById(Long id) {
        Warehouse warehouse = warehouses.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        return ApiResponse.ok(WarehouseResponse.from(warehouse));
    }

    @Override
    @Transactional
    public ApiResponse<?> createWarehouse(WarehouseRequest request) {
        if (warehouses.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("Warehouse with name '" + request.getName().trim() + "' already exists");
        }
        Warehouse warehouse = warehouses.save(Warehouse.builder()
                .name(request.getName().trim())
                .location(request.getLocation().trim())
                .build());
        return ApiResponse.ok("Warehouse created", WarehouseResponse.from(warehouse));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouses.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (!warehouse.getName().equalsIgnoreCase(trimmedName) && warehouses.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Warehouse with name '" + trimmedName + "' already exists");
        }

        warehouse.setName(trimmedName);
        warehouse.setLocation(request.getLocation().trim());
        return ApiResponse.ok("Warehouse updated", WarehouseResponse.from(warehouses.save(warehouse)));
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteWarehouse(Long id) {
        Warehouse warehouse = warehouses.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        warehouses.delete(warehouse);
        return ApiResponse.ok("Warehouse deleted", null);
    }
}

