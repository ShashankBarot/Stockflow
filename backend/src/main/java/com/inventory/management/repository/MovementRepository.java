package com.inventory.management.repository;

import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByType(MovementType type, Pageable pageable);

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<StockMovement> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
