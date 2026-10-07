package com.inventory.management.controller;

import com.inventory.management.dto.request.TransferOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.AuthService;
import com.inventory.management.service.TransferOrderService;
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
@RequestMapping("/transfer-orders")
@RequiredArgsConstructor
public class TransferOrderController {

    private final TransferOrderService transferOrderService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTransferOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transferOrderService.getAllTransferOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getTransferOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(transferOrderService.getTransferOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<?>> createTransferOrder(
            @Valid @RequestBody TransferOrderRequest request,
            Authentication authentication) {
        Long userId = authService.userId(authentication.getName());
        return ResponseEntity.ok(transferOrderService.createTransferOrder(request, userId));
    }
}
