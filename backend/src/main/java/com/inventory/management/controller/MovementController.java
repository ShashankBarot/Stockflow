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

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createMovement(
            @Valid @RequestBody StockMovementRequest request) {
        // TODO: Extract userId from security context
        return ResponseEntity.ok(inventoryService.createMovement(request, 1L));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllMovements(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getAllMovements(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getMovementById(
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getMovementById(id));
    }
}
