package com.inventory.management.service;

import com.inventory.management.dto.request.TransferOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface TransferOrderService {
    ApiResponse<?> getAllTransferOrders(Pageable pageable);
    ApiResponse<?> getTransferOrderById(Long id);
    ApiResponse<?> createTransferOrder(TransferOrderRequest request, Long userId);
}

