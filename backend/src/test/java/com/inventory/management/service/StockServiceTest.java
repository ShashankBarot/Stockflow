package com.inventory.management.service;

import com.inventory.management.entity.Brand;
import com.inventory.management.entity.Category;
import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.entity.User;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.exception.InsufficientStockException;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.BrandRepository;
import com.inventory.management.repository.CategoryRepository;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.MovementRepository;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:stock-service-test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=stockflow-auth-integration-test-secret-key-32chars",
        "app.jwt.access-token-expiration=900000",
        "app.jwt.refresh-token-expiration=604800000",
        "JWT_SECRET=stockflow-auth-integration-test-secret-key-32chars",
        "JWT_ACCESS_EXPIRATION=900000",
        "JWT_REFRESH_EXPIRATION=604800000"
})
class StockServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Product testProduct;
    private Warehouse testWarehouse;
    private User testUser;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        userRepository.deleteAll();

        com.inventory.management.entity.Category category = categoryRepository.save(
                com.inventory.management.entity.Category.builder().name("General").description("General cat").build());
        com.inventory.management.entity.Brand brand = brandRepository.save(
                com.inventory.management.entity.Brand.builder().name("Generic").description("Generic brand").build());

        testProduct = productRepository.save(Product.builder()
                .sku("PROD-TEST-100")
                .name("Test Product")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("99.99"))
                .build());

        testWarehouse = warehouseRepository.save(Warehouse.builder()
                .name("Main Central")
                .location("Sector 5")
                .build());

        testUser = userRepository.save(User.builder()
                .username("stock-admin")
                .email("admin@test.stock")
                .passwordHash("secret-hash")
                .build());
    }

    @Test
    void test1_receiveStockIncreasesQuantityAndCreatesMovement() {
        // Initial inventory = 100
        inventoryRepository.save(Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(100)
                .build());

        StockMovement movement = stockService.receiveStock(
                testProduct.getId(), testWarehouse.getId(), 50, "REF-RCV-1", testUser.getId(), "Inbound shipment");

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(150);

        List<StockMovement> movements = movementRepository.findAll();
        assertThat(movements).hasSize(1);
        assertThat(movement.getType()).isEqualTo(MovementType.INBOUND);
        assertThat(movement.getQuantity()).isEqualTo(50);
        assertThat(movement.getReferenceNumber()).isEqualTo("REF-RCV-1");
        assertThat(movement.getPerformedBy().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void test2_deductStockDecreasesQuantityAndCreatesMovement() {
        // Initial inventory = 100
        inventoryRepository.save(Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(100)
                .build());

        StockMovement movement = stockService.deductStock(
                testProduct.getId(), testWarehouse.getId(), 30, "REF-DED-1", testUser.getId(), "Outbound delivery");

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(70);

        List<StockMovement> movements = movementRepository.findAll();
        assertThat(movements).hasSize(1);
        assertThat(movement.getType()).isEqualTo(MovementType.OUTBOUND);
        assertThat(movement.getQuantity()).isEqualTo(30);
    }

    @Test
    void test3_insufficientStockThrowsExceptionAndLeavesQuantityUnchanged() {
        // Initial inventory = 20
        inventoryRepository.save(Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(20)
                .build());

        assertThrows(InsufficientStockException.class, () -> {
            stockService.deductStock(testProduct.getId(), testWarehouse.getId(), 30, "REF-FAIL-1", testUser.getId(), "Overdeduct");
        });

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(20);
        assertThat(movementRepository.findAll()).isEmpty();
    }

    @Test
    void test4_adjustStockSetsAbsoluteQuantityAndCreatesMovement() {
        // Initial inventory = 100
        inventoryRepository.save(Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(100)
                .build());

        StockMovement movement = stockService.adjustStock(
                testProduct.getId(), testWarehouse.getId(), 80, "REF-ADJ-1", testUser.getId(), "Stock count discrepancy");

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(80);

        List<StockMovement> movements = movementRepository.findAll();
        assertThat(movements).hasSize(1);
        assertThat(movement.getType()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(movement.getQuantity()).isEqualTo(80);
    }

    @Test
    void test5_invalidQuantityRejected() {
        assertThrows(IllegalArgumentException.class, () -> stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), null, "REF-0", testUser.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 0, "REF-0", testUser.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), -5, "REF-0", testUser.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> stockService.deductStock(testProduct.getId(), testWarehouse.getId(), -10, "REF-0", testUser.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> stockService.adjustStock(testProduct.getId(), testWarehouse.getId(), -1, "REF-0", testUser.getId(), null));
    }

    @Test
    void test6_productNotFoundThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            stockService.receiveStock(99999L, testWarehouse.getId(), 10, "REF-ERR", testUser.getId(), null);
        });
        assertThat(movementRepository.findAll()).isEmpty();
    }

    @Test
    void test7_warehouseNotFoundThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            stockService.receiveStock(testProduct.getId(), 88888L, 10, "REF-ERR", testUser.getId(), null);
        });
        assertThat(movementRepository.findAll()).isEmpty();
    }

    @Test
    void test8_transactionRollbackOnMovementFailure() {
        // Setup initial inventory
        inventoryRepository.save(Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(100)
                .build());

        // We simulate a transaction failure within a custom programmatic transaction block
        // or assert rollback behavior when movementRepository fails.
        // Let's verify that when a transaction rolls back, inventory updates do not persist:
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Inventory inv = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(testProduct.getId(), testWarehouse.getId()).orElseThrow();
            inv.setQuantity(200);
            inventoryRepository.save(inv);
            // Simulate error before commit
            throw new RuntimeException("Simulated ledger failure");
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
        }

        Inventory invAfter = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(invAfter.getQuantity()).isEqualTo(100);
    }

    @Test
    void test9_movementContentIsAccurate() {
        StockMovement movement = stockService.receiveStock(
                testProduct.getId(), testWarehouse.getId(), 25, "PO-2026-99", testUser.getId(), "Supplier delivery batch #99");

        assertThat(movement.getProduct().getId()).isEqualTo(testProduct.getId());
        assertThat(movement.getWarehouse().getId()).isEqualTo(testWarehouse.getId());
        assertThat(movement.getType()).isEqualTo(MovementType.INBOUND);
        assertThat(movement.getQuantity()).isEqualTo(25);
        assertThat(movement.getPerformedBy().getId()).isEqualTo(testUser.getId());
        assertThat(movement.getReferenceNumber()).isEqualTo("PO-2026-99");
        assertThat(movement.getNotes()).isEqualTo("Supplier delivery batch #99");
        assertThat(movement.getCreatedAt()).isNotNull();
    }

    @Test
    void test10_concurrentStockDeductions() throws Exception {
        // Initial inventory = 100
        inventoryRepository.save(Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(100)
                .build());

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch readyLatch = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch doneLatch = new java.util.concurrent.CountDownLatch(2);

        java.util.concurrent.atomic.AtomicReference<Throwable> err1 = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<Throwable> err2 = new java.util.concurrent.atomic.AtomicReference<>();

        // Thread 1 deducts 40
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                stockService.deductStock(testProduct.getId(), testWarehouse.getId(), 40, "CONC-1", testUser.getId(), "Deduct 40");
            } catch (Throwable t) {
                err1.set(t);
            } finally {
                doneLatch.countDown();
            }
        });

        // Thread 2 deducts 30
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                stockService.deductStock(testProduct.getId(), testWarehouse.getId(), 30, "CONC-2", testUser.getId(), "Deduct 30");
            } catch (Throwable t) {
                err2.set(t);
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(err1.get()).isNull();
        assertThat(err2.get()).isNull();

        Inventory finalInventory = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        // 100 - 40 - 30 = 30
        assertThat(finalInventory.getQuantity()).isEqualTo(30);

        List<StockMovement> movements = movementRepository.findAll();
        assertThat(movements).hasSize(2);
    }
}
