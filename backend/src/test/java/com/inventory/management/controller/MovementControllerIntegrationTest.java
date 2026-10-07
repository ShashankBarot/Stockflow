package com.inventory.management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.inventory.management.repository.RoleRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        "spring.datasource.url=jdbc:h2:mem:movement-controller-test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
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
class MovementControllerIntegrationTest {

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
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.inventory.management.repository.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String staffToken;
    private Product product;
    private Warehouse warehouse;
    private User staffUser;

    @BeforeEach
    void setUp() throws Exception {
        movementRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        Role staffRole = roleRepository.findByName("STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").build()));

        staffUser = userRepository.findByUsername("movement-staff")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("movement-staff")
                        .email("staff@movements.test")
                        .passwordHash(passwordEncoder.encode("Password-123"))
                        .role(staffRole)
                        .build()));

        Category category = categoryRepository.save(Category.builder().name("General").description("General cat").build());
        Brand brand = brandRepository.save(Brand.builder().name("Generic").description("Generic brand").build());

        product = productRepository.save(Product.builder()
                .sku("MOVE-SKU-100")
                .name("Movement Tested Item")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("49.99"))
                .build());

        warehouse = warehouseRepository.save(Warehouse.builder()
                .name("Central Depot")
                .location("Bay 7")
                .build());

        // Login to get token
        MvcResult loginResult = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "username", "movement-staff",
                                "password", "Password-123"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = mapper.readTree(loginResult.getResponse().getContentAsString());
        staffToken = responseNode.path("data").path("accessToken").asText();
    }

    @Test
    void test1_getMovementsSuccessfully() throws Exception {
        mvc.perform(get("/movements")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void test2_getMovementByIdSuccessfully() throws Exception {
        // Create an inventory item and a movement via POST
        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.INBOUND, 40, "REF-INIT", null, "Init stock");

        MvcResult createResult = mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createdNode = mapper.readTree(createResult.getResponse().getContentAsString());
        long movementId = createdNode.path("data").path("id").asLong();

        mvc.perform(get("/movements/" + movementId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(movementId))
                .andExpect(jsonPath("$.data.quantity").value(40))
                .andExpect(jsonPath("$.data.referenceNumber").value("REF-INIT"));
    }

    @Test
    void test3_getUnknownMovementReturns404() throws Exception {
        mvc.perform(get("/movements/99999")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test4_postReceiveMovementDelegatesToStockService() throws Exception {
        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.INBOUND, 50, "REF-RCV", null, "Goods in");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("INBOUND"))
                .andExpect(jsonPath("$.data.quantity").value(50));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(50);
    }

    @Test
    void test5_postDeductMovementDelegatesToStockService() throws Exception {
        // Seed 60 units first
        inventoryRepository.save(Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(60)
                .build());

        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.OUTBOUND, 25, "REF-DED", null, "Order dispatch");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("OUTBOUND"))
                .andExpect(jsonPath("$.data.quantity").value(25));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(35);
    }

    @Test
    void test6_postAdjustmentDelegatesToStockService() throws Exception {
        inventoryRepository.save(Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(100)
                .build());

        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.ADJUSTMENT, 85, "REF-ADJ", null, "Audit count");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.data.quantity").value(85));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(85);
    }

    @Test
    void test7_invalidQuantityRejectedWith400() throws Exception {
        String badPayload = """
                {
                  "productId": %d,
                  "warehouseId": %d,
                  "type": "INBOUND",
                  "quantity": 0,
                  "referenceNumber": "REF-BAD"
                }
                """.formatted(product.getId(), warehouse.getId());

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test8_missingProductRejectedWith404() throws Exception {
        StockMovementRequest req = new StockMovementRequest(
                99999L, warehouse.getId(), MovementType.INBOUND, 10, "REF-NOPROD", null, null);

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test9_missingWarehouseRejectedWith404() throws Exception {
        StockMovementRequest req = new StockMovementRequest(
                product.getId(), 88888L, MovementType.INBOUND, 10, "REF-NOWH", null, null);

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test10_unsupportedMovementTypeRejectedWith400() throws Exception {
        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.TRANSFER, 10, "REF-TRANS", null, null);

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test11_insufficientStockPropagates400AndNoInventoryMutation() throws Exception {
        inventoryRepository.save(Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(20)
                .build());

        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.OUTBOUND, 30, "REF-OVERDED", null, null);

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(20);
        assertThat(movementRepository.findAll()).isEmpty();
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void test12_duplicateMovementPreventionMandatory() throws Exception {
        // Initial inventory = 100
        inventoryRepository.save(Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(100)
                .build());

        StockMovementRequest req = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.INBOUND, 50, "REF-DUP-CHECK", null, "Audit batch");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Inventory must be 150
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
        assertThat(inv.getQuantity()).isEqualTo(150);

        // Movement count MUST be exactly 1
        List<StockMovement> movements = movementRepository.findAll();
        assertThat(movements).hasSize(1);

        StockMovement record = movements.get(0);
        assertThat(record.getQuantity()).isEqualTo(50);
        assertThat(record.getType()).isEqualTo(MovementType.INBOUND);
        assertThat(record.getPerformedBy().getUsername()).isEqualTo("movement-staff");
    }

    @Test
    void test13_immutableLedgerDisallowsUpdateOrDelete() throws Exception {
        mvc.perform(put("/movements/1").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(patch("/movements/1").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(delete("/movements/1").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void test14_unauthenticatedAccessReturns401() throws Exception {
        mvc.perform(get("/movements"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/movements").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
