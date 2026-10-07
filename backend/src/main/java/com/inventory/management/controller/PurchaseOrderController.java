package com.inventory.management.controller;

import com.inventory.management.dto.request.PurchaseOrderRequest;
import com.inventory.management.dto.request.ReceiveOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.AuthService;
import com.inventory.management.service.PurchaseOrderService;
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
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllPurchaseOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getPurchaseOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(request));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<?>> receivePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveOrderRequest request,
            Authentication authentication) {
        Long userId = authService.userId(authentication.getName());
        return ResponseEntity.ok(purchaseOrderService.receivePurchaseOrder(id, request, userId));
    }
}
