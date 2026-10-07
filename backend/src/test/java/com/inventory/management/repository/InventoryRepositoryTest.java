package com.inventory.management.repository;

import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.Warehouse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inv-repo-test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void pessimisticLockQueryFindsInventorySuccessfully() {
        com.inventory.management.entity.Category category = entityManager.persistAndFlush(
                com.inventory.management.entity.Category.builder().name("General").description("General cat").build());
        com.inventory.management.entity.Brand brand = entityManager.persistAndFlush(
                com.inventory.management.entity.Brand.builder().name("Generic").description("Generic brand").build());

        Product product = Product.builder()
                .sku("SKU-LOCK-TEST")
                .name("Lock Test Product")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("10.00"))
                .build();
        product = entityManager.persistAndFlush(product);

        Warehouse warehouse = Warehouse.builder()
                .name("Lock Warehouse")
                .location("Aisle 1")
                .build();
        warehouse = entityManager.persistAndFlush(warehouse);

        Inventory inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(50)
                .minThreshold(5)
                .build();
        inventory = entityManager.persistAndFlush(inventory);
        entityManager.clear();

        Optional<Inventory> locked = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(product.getId(), warehouse.getId());

        assertThat(locked).isPresent();
        assertThat(locked.get().getQuantity()).isEqualTo(50);
        assertThat(locked.get().getProduct().getId()).isEqualTo(product.getId());
        assertThat(locked.get().getWarehouse().getId()).isEqualTo(warehouse.getId());
    }

    @Test
    void inventoryUniqueConstraintPreventsDuplicateProductWarehousePairs() {
        com.inventory.management.entity.Category category = entityManager.persistAndFlush(
                com.inventory.management.entity.Category.builder().name("General-2").description("General cat 2").build());
        com.inventory.management.entity.Brand brand = entityManager.persistAndFlush(
                com.inventory.management.entity.Brand.builder().name("Generic-2").description("Generic brand 2").build());

        Product product = Product.builder()
                .sku("SKU-DUP-TEST")
                .name("Duplicate Test Product")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("15.00"))
                .build();
        product = entityManager.persistAndFlush(product);

        Warehouse warehouse = Warehouse.builder()
                .name("Dup Warehouse")
                .location("Aisle 2")
                .build();
        warehouse = entityManager.persistAndFlush(warehouse);

        Inventory first = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(10)
                .build();
        entityManager.persistAndFlush(first);

        Inventory duplicate = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(20)
                .build();

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicate);
        });
    }
}

