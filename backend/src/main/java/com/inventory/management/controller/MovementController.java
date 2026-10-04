package com.inventory.management.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

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
import org.springframework.security.core.Authentication;
import com.inventory.management.service.AuthService;

@RestController
@ConditionalOnBean(com.inventory.management.service.InventoryService.class)
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    private final InventoryService inventoryService;
    private final AuthService authService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<?>> createMovement(
            @Valid @RequestBody StockMovementRequest request, Authentication authentication) {
        Long userId = authService.userId(authentication.getName());
        return ResponseEntity.ok(inventoryService.createMovement(request, userId));
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
