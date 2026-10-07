package com.inventory.management.controller;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * InventoryController handles inventory-ledger queries and threshold management.
 *
 * <p>This controller is deferred until Phase B. It requires InventoryService to be
 * implemented before it can load. The @ConditionalOnBean annotation prevents this
 * controller from being registered while InventoryService has no implementation.
 */
@RestController
@ConditionalOnBean(InventoryService.class)
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
