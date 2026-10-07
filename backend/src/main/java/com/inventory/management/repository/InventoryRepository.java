package com.inventory.management.repository;

import com.inventory.management.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Read-only lookup — used for existence checks and read queries.
     * Do NOT use for stock mutations; use findByProductIdAndWarehouseIdForUpdate instead.
     */
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /**
     * Acquires a PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) lock on the inventory row
     * for the given product/warehouse pair.
     *
     * <p>Must ONLY be called within an active @Transactional method. The lock is held
     * until the transaction commits or rolls back. This prevents lost updates under
     * concurrent stock mutations against the same product+warehouse combination.
     *
     * <p>StockService is the sole caller of this method.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.product.id = :productId
              AND i.warehouse.id = :warehouseId
            """)
    Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    List<Inventory> findByWarehouseId(Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    Page<Inventory> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<Inventory> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minThreshold")
    Page<Inventory> findLowStock(Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.quantity <= i.minThreshold")
    Page<Inventory> findLowStockByWarehouse(@Param("warehouseId") Long warehouseId, Pageable pageable);
}
