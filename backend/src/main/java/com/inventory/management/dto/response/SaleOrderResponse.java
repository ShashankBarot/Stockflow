package com.inventory.management.dto.response;

import com.inventory.management.entity.SaleOrder;

import java.time.LocalDateTime;

public record SaleOrderResponse(
        Long id,
        String customerName,
        LocalDateTime orderDate,
        String status,
        String referenceNumber,
        LocalDateTime createdAt
) {
    public static SaleOrderResponse from(SaleOrder order) {
        return new SaleOrderResponse(
                order.getId(),
                order.getCustomer().getName(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getReferenceNumber(),
                order.getCreatedAt()
        );
    }
}

