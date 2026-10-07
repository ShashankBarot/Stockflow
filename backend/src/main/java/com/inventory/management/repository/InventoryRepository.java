package com.inventory.management.repository;

import com.inventory.management.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inventory i WHERE i.quantity < i.minThreshold")
    List<Inventory> findLowStock();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(i.quantity * p.basePrice) FROM Inventory i JOIN i.product p")
    java.math.BigDecimal calculateTotalStockValue();
}
