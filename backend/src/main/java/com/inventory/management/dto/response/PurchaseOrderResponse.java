package com.inventory.management.dto.response;

import com.inventory.management.entity.PurchaseOrder;

import java.time.LocalDateTime;

public record PurchaseOrderResponse(
        Long id,
        String supplierName,
        LocalDateTime orderDate,
        String status,
        String referenceNumber,
        LocalDateTime createdAt
) {
    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return new PurchaseOrderResponse(
                order.getId(),
                order.getSupplier().getName(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getReferenceNumber(),
                order.getCreatedAt()
        );
    }
}

