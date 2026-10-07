package com.inventory.management.service;

import com.inventory.management.dto.request.InventoryThresholdRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.InventoryResponse;
import com.inventory.management.entity.Inventory;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getInventory(Long productId, Long warehouseId, Boolean lowStock, Pageable pageable) {
        Page<Inventory> page;

        if (Boolean.TRUE.equals(lowStock)) {
            if (warehouseId != null) {
                page = inventoryRepository.findLowStockByWarehouse(warehouseId, pageable);
            } else {
                page = inventoryRepository.findLowStock(pageable);
            }
        } else if (productId != null && warehouseId != null) {
            List<Inventory> list = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                    .map(List::of)
                    .orElse(List.of());
            page = new PageImpl<>(list, pageable, list.size());
        } else if (warehouseId != null) {
            page = inventoryRepository.findByWarehouseId(warehouseId, pageable);
        } else if (productId != null) {
            page = inventoryRepository.findByProductId(productId, pageable);
        } else {
            page = inventoryRepository.findAll(pageable);
        }

        return ApiResponse.ok(page.map(InventoryResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getInventoryByWarehouse(Long warehouseId, Pageable pageable) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse not found with id: " + warehouseId);
        }
        Page<Inventory> page = inventoryRepository.findByWarehouseId(warehouseId, pageable);
        return ApiResponse.ok(page.map(InventoryResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return ApiResponse.ok(InventoryResponse.from(inventory));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateInventoryThreshold(Long id, InventoryThresholdRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Threshold request cannot be null");
        }
        if (request.getMinThreshold() == null || request.getMinThreshold() < 0) {
            throw new IllegalArgumentException("Minimum threshold cannot be negative or null");
        }
        if (request.getMaxCapacity() != null && request.getMaxCapacity() < 0) {
            throw new IllegalArgumentException("Maximum capacity cannot be negative");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        inventory.setMinThreshold(request.getMinThreshold());
        if (request.getMaxCapacity() != null) {
            inventory.setMaxCapacity(request.getMaxCapacity());
        }

        // NOTE: ONLY minThreshold and maxCapacity are modified. quantity is NOT modified.
        Inventory updated = inventoryRepository.save(inventory);

        return ApiResponse.ok("Inventory threshold updated", InventoryResponse.from(updated));
    }
}

