package com.inventory.management.controller;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getInventory(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getInventory(pageable));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<?>> getInventoryByWarehouse(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
    }

    @PutMapping("/{id}/threshold")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateInventoryThreshold(
            @PathVariable Long id,
            @RequestBody Object request) {
        return ResponseEntity.ok(inventoryService.updateInventoryThreshold(id, request));
    }
}
