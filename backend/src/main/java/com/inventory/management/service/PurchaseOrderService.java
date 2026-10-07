package com.inventory.management.service;

import com.inventory.management.dto.request.PurchaseOrderRequest;
import com.inventory.management.dto.request.ReceiveOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface PurchaseOrderService {
    ApiResponse<?> getAllPurchaseOrders(Pageable pageable);
    ApiResponse<?> getPurchaseOrderById(Long id);
    ApiResponse<?> createPurchaseOrder(PurchaseOrderRequest request);
    ApiResponse<?> receivePurchaseOrder(Long id, ReceiveOrderRequest request, Long userId);
}

