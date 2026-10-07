package com.inventory.management.controller;

import com.inventory.management.dto.request.DispatchOrderRequest;
import com.inventory.management.dto.request.SaleOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.AuthService;
import com.inventory.management.service.SaleOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import com.inventory.management.service.InventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnBean(InventoryService.class)
@RequestMapping("/sale-orders")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllSaleOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(saleOrderService.getAllSaleOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getSaleOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(saleOrderService.getSaleOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> createSaleOrder(
            @Valid @RequestBody SaleOrderRequest request) {
        return ResponseEntity.ok(saleOrderService.createSaleOrder(request));
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<?>> dispatchSaleOrder(
            @PathVariable Long id,
            @Valid @RequestBody DispatchOrderRequest request,
            Authentication authentication) {
        Long userId = authService.userId(authentication.getName());
        return ResponseEntity.ok(saleOrderService.dispatchSaleOrder(id, request, userId));
    }
}
