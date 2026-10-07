package com.inventory.management.repository;

import com.inventory.management.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findWithRelationsById(@Param("id") Long id);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.brand WHERE " +
            "(:category IS NULL OR :category = '' OR LOWER(p.category.name) = LOWER(:category)) AND " +
            "(:brand IS NULL OR :brand = '' OR LOWER(p.brand.name) = LOWER(:brand)) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE " +
            "(:category IS NULL OR :category = '' OR LOWER(p.category.name) = LOWER(:category)) AND " +
            "(:brand IS NULL OR :brand = '' OR LOWER(p.brand.name) = LOWER(:brand)) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search,
                                 @Param("category") String category,
                                 @Param("brand") String brand,
                                 Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.brand WHERE " +
            "(:category IS NULL OR :category = '' OR LOWER(p.category.name) = LOWER(:category)) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE " +
            "(:category IS NULL OR :category = '' OR LOWER(p.category.name) = LOWER(:category)) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search,
                                 @Param("category") String category,
                                 Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.brand",
            countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithRelations(Pageable pageable);
}
