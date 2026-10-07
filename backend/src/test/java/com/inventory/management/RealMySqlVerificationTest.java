package com.inventory.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.dto.request.InventoryThresholdRequest;
import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.entity.Brand;
import com.inventory.management.entity.Category;
import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.Role;
import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.entity.User;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.repository.BrandRepository;
import com.inventory.management.repository.CategoryRepository;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.MovementRepository;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.RefreshTokenRepository;
import com.inventory.management.repository.RoleRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.repository.WarehouseRepository;
import com.inventory.management.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/stock_management?useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true",
        "spring.datasource.username=root",
        "spring.datasource.password=\\$Ha@456789",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
        "app.jwt.secret=stockflow-mysql-production-verification-test-secret-32-chars",
        "app.jwt.access-token-expiration=900000",
        "app.jwt.refresh-token-expiration=604800000",
        "JWT_SECRET=stockflow-mysql-production-verification-test-secret-32-chars",
        "JWT_ACCESS_EXPIRATION=900000",
        "JWT_REFRESH_EXPIRATION=604800000"
})
class RealMySqlVerificationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String adminToken;
    private String staffToken;
    private Product testProduct;
    private Warehouse testWarehouse;
    private User staffUser;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        Role staffRole = roleRepository.findByName("STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").build()));

        User adminUser = userRepository.findByUsername("mysql-admin")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("mysql-admin")
                        .email("admin@mysql.test")
                        .passwordHash(passwordEncoder.encode("AdminPass-123"))
                        .role(adminRole)
                        .build()));

        staffUser = userRepository.findByUsername("mysql-staff")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("mysql-staff")
                        .email("staff@mysql.test")
                        .passwordHash(passwordEncoder.encode("StaffPass-123"))
                        .role(staffRole)
                        .build()));

        Category testCategory = categoryRepository.findByName("General")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("General").description("General category").build()));
        Brand testBrand = brandRepository.findByName("Generic")
                .orElseGet(() -> brandRepository.save(Brand.builder().name("Generic").description("Generic brand").build()));

        // Use distinct SKUs per run to preserve deterministic isolation
        String suffix = String.valueOf(System.currentTimeMillis() % 100000);
        testProduct = productRepository.save(Product.builder()
                .sku("MYSQL-PROD-" + suffix)
                .name("MySQL Live Product " + suffix)
                .category(testCategory)
                .brand(testBrand)
                .basePrice(new BigDecimal("129.99"))
                .build());

        testWarehouse = warehouseRepository.save(Warehouse.builder()
                .name("MySQL Warehouse " + suffix)
                .location("Aisle 9B")
                .build());

        adminToken = loginAndGetToken("mysql-admin", "AdminPass-123");
        staffToken = loginAndGetToken("mysql-staff", "StaffPass-123");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = mapper.readTree(res.getResponse().getContentAsString());
        return node.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("1. Real MySQL Authentication and Current User verification")
    void test01_authenticationAgainstRealMySql() throws Exception {
        mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("mysql-admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("2. Master Data Product & Warehouse CRUD against real MySQL")
    void test02_masterDataPersistence() throws Exception {
        assertThat(testProduct.getId()).isNotNull();
        assertThat(testWarehouse.getId()).isNotNull();

        // Query product via endpoint
        mvc.perform(get("/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value(testProduct.getSku()));

        // Query warehouse via endpoint
        mvc.perform(get("/warehouses/" + testWarehouse.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(testWarehouse.getName()));
    }

    @Test
    @DisplayName("3. Real MySQL Stock Inbound Mutation & Persistence")
    void test03_stockReceiveFlow() throws Exception {
        StockMovementRequest req = new StockMovementRequest(
                testProduct.getId(), testWarehouse.getId(), MovementType.INBOUND, 100, "MYSQL-RCV-1", null, "Initial MySQL stock");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(100));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("4. Real MySQL Complete Movement Flow (Inbound, Outbound, Adjust)")
    void test04_completeMovementLifecycle() throws Exception {
        // Step 1: Inbound 100
        StockMovementRequest rcv = new StockMovementRequest(
                testProduct.getId(), testWarehouse.getId(), MovementType.INBOUND, 100, "FLOW-1", null, null);
        mvc.perform(post("/movements").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(rcv)))
                .andExpect(status().isOk());

        // Step 2: Outbound 30 -> 70
        StockMovementRequest ded = new StockMovementRequest(
                testProduct.getId(), testWarehouse.getId(), MovementType.OUTBOUND, 30, "FLOW-2", null, null);
        mvc.perform(post("/movements").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(ded)))
                .andExpect(status().isOk());

        Inventory afterDed = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(afterDed.getQuantity()).isEqualTo(70);

        // Step 3: Adjust 120 -> 120
        StockMovementRequest adj = new StockMovementRequest(
                testProduct.getId(), testWarehouse.getId(), MovementType.ADJUSTMENT, 120, "FLOW-3", null, null);
        mvc.perform(post("/movements").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(adj)))
                .andExpect(status().isOk());

        Inventory afterAdj = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(afterAdj.getQuantity()).isEqualTo(120);
    }

    @Test
    @DisplayName("5. Mandatory Invariant: Exactly ONE StockMovement per operation in MySQL")
    void test05_exactlyOneMovementPerMutation() {
        int initialCount = movementRepository.findByProductId(testProduct.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().size();

        // 1. Receive 50
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 50, "REF-EXACT-1", staffUser.getId(), null);
        int afterRcv = movementRepository.findByProductId(testProduct.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().size();
        assertThat(afterRcv).isEqualTo(initialCount + 1);

        // 2. Deduct 20
        stockService.deductStock(testProduct.getId(), testWarehouse.getId(), 20, "REF-EXACT-2", staffUser.getId(), null);
        int afterDed = movementRepository.findByProductId(testProduct.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().size();
        assertThat(afterDed).isEqualTo(afterRcv + 1);

        // 3. Adjust 40
        stockService.adjustStock(testProduct.getId(), testWarehouse.getId(), 40, "REF-EXACT-3", staffUser.getId(), null);
        int afterAdj = movementRepository.findByProductId(testProduct.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().size();
        assertThat(afterAdj).isEqualTo(afterDed + 1);
    }

    @Test
    @DisplayName("6. Insufficient Stock Rejection & Rollback in real MySQL")
    void test06_insufficientStockRollback() throws Exception {
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 20, "INIT-20", staffUser.getId(), null);

        int countBefore = movementRepository.findAll().size();

        StockMovementRequest overDeduct = new StockMovementRequest(
                testProduct.getId(), testWarehouse.getId(), MovementType.OUTBOUND, 30, "OVER-FAIL", null, null);

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(overDeduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(20); // Quantity unchanged
        assertThat(movementRepository.findAll().size()).isEqualTo(countBefore); // No movement committed
    }

    @Test
    @DisplayName("7. Transaction Rollback Integrity in real MySQL")
    void test07_transactionRollbackIntegrity() {
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 100, "ROLL-INIT", staffUser.getId(), null);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Inventory inv = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(testProduct.getId(), testWarehouse.getId()).orElseThrow();
            inv.setQuantity(999);
            inventoryRepository.save(inv);
            throw new RuntimeException("Simulated mid-transaction failure");
        } catch (Exception e) {
            transactionManager.rollback(status);
        }

        Inventory invAfter = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        assertThat(invAfter.getQuantity()).isEqualTo(100); // Rolled back cleanly
    }

    @Test
    @DisplayName("8. Real MySQL Pessimistic Locking & Concurrent Deductions")
    void test08_pessimisticLockingConcurrencyInMySql() throws Exception {
        // Initial inventory = 100
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 100, "CONC-INIT", staffUser.getId(), null);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicReference<Throwable> err1 = new AtomicReference<>();
        AtomicReference<Throwable> err2 = new AtomicReference<>();

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                stockService.deductStock(testProduct.getId(), testWarehouse.getId(), 40, "CONC-TH-1", staffUser.getId(), null);
            } catch (Throwable t) {
                err1.set(t);
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                stockService.deductStock(testProduct.getId(), testWarehouse.getId(), 30, "CONC-TH-2", staffUser.getId(), null);
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

        Inventory finalInv = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        // 100 - 40 - 30 = 30
        assertThat(finalInv.getQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("9. Real MySQL Unique Constraint (product_id, warehouse_id)")
    void test09_uniqueConstraintEnforcementInMySql() {
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 10, "UQ-1", staffUser.getId(), null);

        Inventory duplicate = Inventory.builder()
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(5)
                .build();

        assertThrows(Exception.class, () -> {
            inventoryRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("10. Threshold update metadata-only in real MySQL")
    void test10_thresholdUpdateMetadataOnly() throws Exception {
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 100, "TH-INIT", staffUser.getId(), null);
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();

        int movementCountBefore = movementRepository.findAll().size();

        InventoryThresholdRequest req = new InventoryThresholdRequest(35, 300);
        mvc.perform(put("/inventory/" + inv.getId() + "/threshold")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.minThreshold").value(35))
                .andExpect(jsonPath("$.data.maxCapacity").value(300))
                .andExpect(jsonPath("$.data.quantity").value(100)); // Quantity untouched

        Inventory after = inventoryRepository.findById(inv.getId()).orElseThrow();
        assertThat(after.getMinThreshold()).isEqualTo(35);
        assertThat(after.getQuantity()).isEqualTo(100);
        assertThat(movementRepository.findAll().size()).isEqualTo(movementCountBefore); // No movement created
    }

    @Test
    @DisplayName("11. Low stock detection in real MySQL")
    void test11_lowStockDetectionInMySql() throws Exception {
        stockService.receiveStock(testProduct.getId(), testWarehouse.getId(), 10, "LOW-1", staffUser.getId(), null);
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testWarehouse.getId()).orElseThrow();
        inv.setMinThreshold(20); // Quantity(10) <= MinThreshold(20)
        inventoryRepository.saveAndFlush(inv);

        mvc.perform(get("/inventory?lowStock=true&warehouseId=" + testWarehouse.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(inv.getId()));
    }

    @Test
    @DisplayName("12. Ledger immutability (HTTP 405) in real MySQL environment")
    void test12_ledgerImmutability() throws Exception {
        mvc.perform(put("/movements/1").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(patch("/movements/1").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(delete("/movements/1").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isMethodNotAllowed());
    }
}
