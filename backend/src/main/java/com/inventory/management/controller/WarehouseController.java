package com.inventory.management.controller;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllWarehouses(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getInventory(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> createWarehouse(
            @RequestBody Object request) {
        return ResponseEntity.ok(inventoryService.createProduct(request));
    }
}
