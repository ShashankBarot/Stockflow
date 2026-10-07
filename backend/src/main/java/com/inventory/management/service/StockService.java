package com.inventory.management.service;

import com.inventory.management.entity.StockMovement;

public interface StockService {

    StockMovement receiveStock(Long productId, Long warehouseId, Integer quantity, String referenceNumber, Long performedByUserId, String notes);

    StockMovement deductStock(Long productId, Long warehouseId, Integer quantity, String referenceNumber, Long performedByUserId, String notes);

    StockMovement adjustStock(Long productId, Long warehouseId, Integer newQuantity, String referenceNumber, Long performedByUserId, String notes);
}

