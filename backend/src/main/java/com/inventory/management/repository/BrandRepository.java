package com.inventory.management.repository;

import com.inventory.management.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);

    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

