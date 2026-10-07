package com.inventory.management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.dto.request.BrandRequest;
import com.inventory.management.dto.request.CategoryRequest;
import com.inventory.management.dto.request.CustomerRequest;
import com.inventory.management.dto.request.ProductRequest;
import com.inventory.management.dto.request.SupplierRequest;
import com.inventory.management.dto.request.WarehouseRequest;
import com.inventory.management.entity.Brand;
import com.inventory.management.entity.Category;
import com.inventory.management.entity.Customer;
import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.Role;
import com.inventory.management.entity.Supplier;
import com.inventory.management.entity.User;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.repository.BrandRepository;
import com.inventory.management.repository.CategoryRepository;
import com.inventory.management.repository.CustomerRepository;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.MovementRepository;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.RefreshTokenRepository;
import com.inventory.management.repository.RoleRepository;
import com.inventory.management.repository.SupplierRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.repository.WarehouseRepository;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:master-data-test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
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
class MasterDataIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

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

    private String adminToken;
    private String managerToken;
    private String staffToken;

    @BeforeEach
    void setUp() throws Exception {
        movementRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        supplierRepository.deleteAll();
        customerRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("MANAGER").build()));
        Role staffRole = roleRepository.findByName("STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").build()));

        userRepository.save(User.builder()
                .username("master-admin")
                .email("admin@master.test")
                .passwordHash(passwordEncoder.encode("Password-123"))
                .role(adminRole)
                .build());

        userRepository.save(User.builder()
                .username("master-manager")
                .email("manager@master.test")
                .passwordHash(passwordEncoder.encode("Password-123"))
                .role(managerRole)
                .build());

        userRepository.save(User.builder()
                .username("master-staff")
                .email("staff@master.test")
                .passwordHash(passwordEncoder.encode("Password-123"))
                .role(staffRole)
                .build());

        adminToken = loginAndGetToken("master-admin", "Password-123");
        managerToken = loginAndGetToken("master-manager", "Password-123");
        staffToken = loginAndGetToken("master-staff", "Password-123");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.path("data").path("accessToken").asText();
    }

    // ==========================================
    // 1. CATEGORY MANAGEMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Category: ADMIN and MANAGER can create and update, STAFF is forbidden, ADMIN can delete")
    void testCategoryCrudAndRbac() throws Exception {
        CategoryRequest createReq = new CategoryRequest();
        createReq.setName("Electronics");
        createReq.setDescription("Gadgets and devices");

        // STAFF cannot create -> 403
        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());

        // ADMIN creates successfully
        MvcResult createRes = mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Electronics"))
                .andReturn();

        Long categoryId = mapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Duplicate name fails -> 400
        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // GET by ID (STAFF allowed)
        mvc.perform(get("/categories/" + categoryId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Electronics"));

        // MANAGER updates category
        CategoryRequest updateReq = new CategoryRequest();
        updateReq.setName("Consumer Electronics");
        updateReq.setDescription("Updated description");

        mvc.perform(put("/categories/" + categoryId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Consumer Electronics"));

        // Search categories
        mvc.perform(get("/categories?search=Consumer")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Consumer Electronics"));

        // MANAGER cannot delete -> 403
        mvc.perform(delete("/categories/" + categoryId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());

        // ADMIN deletes -> 200
        mvc.perform(delete("/categories/" + categoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    // ==========================================
    // 2. BRAND MANAGEMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Brand: MANAGER can create, ADMIN can delete, duplicate brand name rejected")
    void testBrandCrudAndValidation() throws Exception {
        BrandRequest createReq = new BrandRequest();
        createReq.setName("Acme Corp");
        createReq.setDescription("Top manufacturer");

        // MANAGER creates
        MvcResult res = mvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Acme Corp"))
                .andReturn();

        Long brandId = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Duplicate name rejected
        mvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest());

        // ADMIN updates
        BrandRequest updateReq = new BrandRequest();
        updateReq.setName("Acme Global");
        updateReq.setDescription("Global leader");

        mvc.perform(put("/brands/" + brandId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Acme Global"));

        // Search
        mvc.perform(get("/brands?search=Global")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Acme Global"));

        // ADMIN deletes
        mvc.perform(delete("/brands/" + brandId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(brandRepository.findById(brandId)).isEmpty();
    }

    // ==========================================
    // 3. SUPPLIER MANAGEMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Supplier: CRUD and search operations work with RBAC")
    void testSupplierCrudAndSearch() throws Exception {
        SupplierRequest createReq = new SupplierRequest();
        createReq.setName("Mega Supplies Ltd");
        createReq.setContactPerson("Alice Green");
        createReq.setEmail("alice@megasupplies.test");
        createReq.setPhone("+1-555-0199");
        createReq.setAddress("42 Industrial Ave");

        MvcResult res = mvc.perform(post("/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Mega Supplies Ltd"))
                .andExpect(jsonPath("$.data.contactPerson").value("Alice Green"))
                .andReturn();

        Long supplierId = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // MANAGER updates
        createReq.setContactPerson("Bob Blue");
        mvc.perform(put("/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactPerson").value("Bob Blue"));

        // Search
        mvc.perform(get("/suppliers?search=Mega")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Mega Supplies Ltd"));

        // ADMIN deletes
        mvc.perform(delete("/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(supplierRepository.findById(supplierId)).isEmpty();
    }

    // ==========================================
    // 4. CUSTOMER MANAGEMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Customer: CRUD and search operations work with RBAC")
    void testCustomerCrudAndSearch() throws Exception {
        CustomerRequest createReq = new CustomerRequest();
        createReq.setName("Global Retail Inc");
        createReq.setEmail("orders@globalretail.test");
        createReq.setPhone("+1-555-0288");
        createReq.setAddress("100 Market St");

        MvcResult res = mvc.perform(post("/customers")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Global Retail Inc"))
                .andReturn();

        Long customerId = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // ADMIN updates
        createReq.setName("Global Retail Superstores");
        mvc.perform(put("/customers/" + customerId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Global Retail Superstores"));

        // Search
        mvc.perform(get("/customers?search=Retail")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Global Retail Superstores"));

        // ADMIN deletes
        mvc.perform(delete("/customers/" + customerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(customerRepository.findById(customerId)).isEmpty();
    }

    // ==========================================
    // 5. PRODUCT MANAGEMENT & FOREIGN KEY PROTECTION
    // ==========================================

    // ==========================================
    // 5. PRODUCT MANAGEMENT & RELATIONSHIPS
    // ==========================================

    @Test
    @DisplayName("Product: Category & Brand relationships, validation, search, 404s, and invariant protection")
    void testProductManagementAndRelationships() throws Exception {
        Category cat1 = categoryRepository.save(Category.builder().name("Electronics").description("Gadgets").build());
        Category cat2 = categoryRepository.save(Category.builder().name("Computers").description("PCs").build());
        Brand brd1 = brandRepository.save(Brand.builder().name("Apple").description("Apple Inc").build());
        Brand brd2 = brandRepository.save(Brand.builder().name("Dell").description("Dell Inc").build());

        // 1. Nonexistent category -> 404
        ProductRequest invalidCatReq = new ProductRequest();
        invalidCatReq.setSku("PROD-INVALID-CAT");
        invalidCatReq.setName("Invalid Cat Product");
        invalidCatReq.setCategoryId(9999L);
        invalidCatReq.setBrandId(brd1.getId());
        invalidCatReq.setBasePrice(new BigDecimal("100.00"));

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidCatReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // 2. Nonexistent brand -> 404
        ProductRequest invalidBrdReq = new ProductRequest();
        invalidBrdReq.setSku("PROD-INVALID-BRD");
        invalidBrdReq.setName("Invalid Brd Product");
        invalidBrdReq.setCategoryId(cat1.getId());
        invalidBrdReq.setBrandId(9999L);
        invalidBrdReq.setBasePrice(new BigDecimal("100.00"));

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidBrdReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Create Product with valid Category + Brand succeeds
        ProductRequest req1 = new ProductRequest();
        req1.setSku("PROD-PHONE-01");
        req1.setName("Smart Phone Pro");
        req1.setDescription("Flagship phone");
        req1.setCategoryId(cat1.getId());
        req1.setBrandId(brd1.getId());
        req1.setBasePrice(new BigDecimal("799.99"));

        MvcResult r1 = mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryId").value(cat1.getId()))
                .andExpect(jsonPath("$.data.categoryName").value("Electronics"))
                .andExpect(jsonPath("$.data.brandId").value(brd1.getId()))
                .andExpect(jsonPath("$.data.brandName").value("Apple"))
                .andReturn();

        Long prod1Id = mapper.readTree(r1.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 4. Duplicate SKU rejected -> 400
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Create second product
        ProductRequest req2 = new ProductRequest();
        req2.setSku("PROD-TABLET-01");
        req2.setName("Smart Tablet Air");
        req2.setCategoryId(cat2.getId());
        req2.setBrandId(brd2.getId());
        req2.setBasePrice(new BigDecimal("499.99"));

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value(cat2.getId()))
                .andExpect(jsonPath("$.data.categoryName").value("Computers"))
                .andExpect(jsonPath("$.data.brandId").value(brd2.getId()))
                .andExpect(jsonPath("$.data.brandName").value("Dell"));

        // 5. Update relationship works
        req1.setCategoryId(cat2.getId());
        req1.setBrandId(brd2.getId());
        req1.setName("Updated Phone Pro");
        mvc.perform(put("/products/" + prod1Id)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value(cat2.getId()))
                .andExpect(jsonPath("$.data.categoryName").value("Computers"))
                .andExpect(jsonPath("$.data.brandId").value(brd2.getId()))
                .andExpect(jsonPath("$.data.brandName").value("Dell"));

        // 6. Filter by category
        mvc.perform(get("/products?category=Computers")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));

        // 7. Filter by brand
        mvc.perform(get("/products?brand=Dell")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));

        // 8. Search by query (SKU or name)
        mvc.perform(get("/products?search=Tablet")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("PROD-TABLET-01"));

        // 9. Verify product creation does NOT create Inventory
        assertThat(inventoryRepository.findAll()).isEmpty();

        // 10. Verify product creation does NOT create StockMovement
        assertThat(movementRepository.findAll()).isEmpty();

        // 11. Foreign key constraint protection on delete
        Warehouse w = warehouseRepository.save(Warehouse.builder().name("Test Wh").location("Loc").build());
        Product p1 = productRepository.findById(prod1Id).orElseThrow();
        inventoryRepository.save(Inventory.builder()
                .product(p1)
                .warehouse(w)
                .quantity(50)
                .minThreshold(5)
                .maxCapacity(100)
                .build());

        // Deleting prod1 should fail with 409 Conflict due to FK reference from Inventory
        mvc.perform(delete("/products/" + prod1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // Delete inventory first, then prod1 deletion succeeds
        inventoryRepository.deleteAll();
        mvc.perform(delete("/products/" + prod1Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(productRepository.findById(prod1Id)).isEmpty();
    }

    // ==========================================
    // 6. WAREHOUSE MANAGEMENT & FOREIGN KEY PROTECTION
    // ==========================================

    @Test
    @DisplayName("Warehouse: search, update, delete, and foreign-key constraint protection")
    void testWarehouseManagementAndFkProtection() throws Exception {
        WarehouseRequest createReq = new WarehouseRequest();
        createReq.setName("Central Hub");
        createReq.setLocation("Zone 4");

        MvcResult res = mvc.perform(post("/warehouses")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Central Hub"))
                .andReturn();

        Long whId = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Update warehouse
        WarehouseRequest updateReq = new WarehouseRequest();
        updateReq.setName("Central Distribution Hub");
        updateReq.setLocation("Zone 4B");

        mvc.perform(put("/warehouses/" + whId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Central Distribution Hub"));

        // Search warehouse
        mvc.perform(get("/warehouses?search=Distribution")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Central Distribution Hub"));

        // Create product & inventory linked to warehouse
        Category defaultCat = categoryRepository.save(Category.builder().name("WhCat").description("Wh Cat").build());
        Brand defaultBrd = brandRepository.save(Brand.builder().name("WhBrd").description("Wh Brd").build());
        Product p = productRepository.save(Product.builder()
                .sku("SKU-WH-TEST")
                .name("Wh Test Prod")
                .category(defaultCat)
                .brand(defaultBrd)
                .basePrice(BigDecimal.TEN)
                .build());
        Warehouse w = warehouseRepository.findById(whId).orElseThrow();
        inventoryRepository.save(Inventory.builder()
                .product(p)
                .warehouse(w)
                .quantity(10)
                .minThreshold(2)
                .maxCapacity(50)
                .build());

        // Deleting warehouse referenced in inventory fails with 409 Conflict
        mvc.perform(delete("/warehouses/" + whId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // Delete inventory first, then warehouse deletion succeeds
        inventoryRepository.deleteAll();
        mvc.perform(delete("/warehouses/" + whId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(warehouseRepository.findById(whId)).isEmpty();
    }
}

