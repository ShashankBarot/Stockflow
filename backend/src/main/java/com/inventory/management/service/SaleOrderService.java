package com.inventory.management.service;

import com.inventory.management.dto.request.DispatchOrderRequest;
import com.inventory.management.dto.request.SaleOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface SaleOrderService {
    ApiResponse<?> getAllSaleOrders(Pageable pageable);
    ApiResponse<?> getSaleOrderById(Long id);
    ApiResponse<?> createSaleOrder(SaleOrderRequest request);
    ApiResponse<?> dispatchSaleOrder(Long id, DispatchOrderRequest request, Long userId);
}

