package com.inventory.management.dto.response;

import com.inventory.management.entity.TransferOrder;

import java.time.LocalDateTime;

public record TransferOrderResponse(
        Long id,
        String sourceWarehouseName,
        String destinationWarehouseName,
        LocalDateTime transferDate,
        String status,
        String referenceNumber,
        LocalDateTime createdAt
) {
    public static TransferOrderResponse from(TransferOrder order) {
        return new TransferOrderResponse(
                order.getId(),
                order.getSourceWarehouse().getName(),
                order.getDestinationWarehouse().getName(),
                order.getTransferDate(),
                order.getStatus().name(),
                order.getReferenceNumber(),
                order.getCreatedAt()
        );
    }
}

