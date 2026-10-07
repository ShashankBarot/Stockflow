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
    public ApiResponse<?> getAllWarehouses(Pageable pageable) {
        return ApiResponse.ok(warehouses.findAll(pageable).map(WarehouseResponse::from));
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
        if (warehouses.existsByName(request.getName())) {
            throw new IllegalArgumentException("Warehouse with name '" + request.getName() + "' already exists");
        }
        Warehouse warehouse = warehouses.save(Warehouse.builder()
                .name(request.getName().trim())
                .location(request.getLocation().trim())
                .build());
        return ApiResponse.ok("Warehouse created", WarehouseResponse.from(warehouse));
    }
}

