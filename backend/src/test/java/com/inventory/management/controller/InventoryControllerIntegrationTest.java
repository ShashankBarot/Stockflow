package com.inventory.management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.dto.request.InventoryThresholdRequest;
import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.entity.Brand;
import com.inventory.management.entity.Category;
import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.Role;
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
        "spring.datasource.url=jdbc:h2:mem:inventory-controller-test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
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
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

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

    private String staffToken;
    private String adminToken;
    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;

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

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        Role staffRole = roleRepository.findByName("STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").build()));

        User adminUser = userRepository.findByUsername("inv-admin")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("inv-admin")
                        .email("admin@inv.test")
                        .passwordHash(passwordEncoder.encode("Password-123"))
                        .role(adminRole)
                        .build()));

        User staffUser = userRepository.findByUsername("inv-staff")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("inv-staff")
                        .email("staff@inv.test")
                        .passwordHash(passwordEncoder.encode("Password-123"))
                        .role(staffRole)
                        .build()));

        Category category = categoryRepository.save(Category.builder().name("General").description("General cat").build());
        Brand brand = brandRepository.save(Brand.builder().name("Generic").description("Generic brand").build());

        product = productRepository.save(Product.builder()
                .sku("SKU-INV-TEST")
                .name("Test Inventory Product")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("19.99"))
                .build());

        warehouse = warehouseRepository.save(Warehouse.builder()
                .name("Depot South")
                .location("Section D")
                .build());

        inventory = inventoryRepository.save(Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(100)
                .minThreshold(10)
                .maxCapacity(200)
                .build());

        adminToken = loginAndGetToken("inv-admin", "Password-123");
        staffToken = loginAndGetToken("inv-staff", "Password-123");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.path("data").path("accessToken").asText();
    }

    @Test
    void test1_getAllInventoryAsStaff() throws Exception {
        mvc.perform(get("/inventory")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].quantity").value(100))
                .andExpect(jsonPath("$.data.content[0].product.sku").value("SKU-INV-TEST"));
    }

    @Test
    void test2_getInventoryByWarehouse() throws Exception {
        mvc.perform(get("/inventory/warehouse/" + warehouse.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].warehouse.name").value("Depot South"));
    }

    @Test
    void test3_getInventoryById() throws Exception {
        mvc.perform(get("/inventory/" + inventory.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inventory.getId()))
                .andExpect(jsonPath("$.data.quantity").value(100));
    }

    @Test
    void test4_getInventoryByIdNotFound() throws Exception {
        mvc.perform(get("/inventory/99999")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test5_updateThresholdAsAdminSuccess() throws Exception {
        InventoryThresholdRequest req = new InventoryThresholdRequest(25, 250);

        mvc.perform(put("/inventory/" + inventory.getId() + "/threshold")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.minThreshold").value(25))
                .andExpect(jsonPath("$.data.maxCapacity").value(250))
                .andExpect(jsonPath("$.data.quantity").value(100)); // Quantity untouched

        Inventory updated = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertThat(updated.getMinThreshold()).isEqualTo(25);
        assertThat(updated.getMaxCapacity()).isEqualTo(250);
        assertThat(updated.getQuantity()).isEqualTo(100);

        // Threshold update must NOT create a stock movement
        assertThat(movementRepository.findAll()).isEmpty();
    }

    @Test
    void test6_updateThresholdAsStaffForbidden() throws Exception {
        InventoryThresholdRequest req = new InventoryThresholdRequest(30, 300);

        mvc.perform(put("/inventory/" + inventory.getId() + "/threshold")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void test7_updateThresholdNegativeRejected() throws Exception {
        String badPayload = "{\"minThreshold\": -1, \"maxCapacity\": 100}";

        mvc.perform(put("/inventory/" + inventory.getId() + "/threshold")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void test8_crossApiConsistencyMovementMutationsReflectedInInventory() throws Exception {
        // Initial state: inventory = 100

        // 1. Inbound movement (+50)
        StockMovementRequest inReq = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.INBOUND, 50, "REF-CROSS-1", null, "Cross test in");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inReq)))
                .andExpect(status().isOk());

        // 2. Query inventory API -> must be 150
        mvc.perform(get("/inventory/" + inventory.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(150));

        // 3. Outbound movement (-30)
        StockMovementRequest outReq = new StockMovementRequest(
                product.getId(), warehouse.getId(), MovementType.OUTBOUND, 30, "REF-CROSS-2", null, "Cross test out");

        mvc.perform(post("/movements")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(outReq)))
                .andExpect(status().isOk());

        // 4. Query inventory API -> must be 120
        mvc.perform(get("/inventory/" + inventory.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(120));

        // 5. Query movement history -> both movements exist
        mvc.perform(get("/movements")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void test9_immutableInventoryNoArbitraryQuantityUpdateEndpoints() throws Exception {
        mvc.perform(post("/inventory").header("Authorization", "Bearer " + adminToken).content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(put("/inventory/" + inventory.getId()).header("Authorization", "Bearer " + adminToken).content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(patch("/inventory/" + inventory.getId()).header("Authorization", "Bearer " + adminToken).content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(delete("/inventory/" + inventory.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void test10_unauthenticatedAccessToInventoryReturns401() throws Exception {
        mvc.perform(get("/inventory"))
                .andExpect(status().isUnauthorized());

        mvc.perform(put("/inventory/1/threshold").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
