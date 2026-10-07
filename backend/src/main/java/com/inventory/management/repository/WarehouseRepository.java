package com.inventory.management.repository;

import com.inventory.management.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByName(String name);

    Page<Warehouse> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Warehouse> findByLocationContainingIgnoreCase(String location, Pageable pageable);
}
