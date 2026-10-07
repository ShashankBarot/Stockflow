package com.inventory.management;

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
import com.inventory.management.entity.Product;
import com.inventory.management.entity.Role;
import com.inventory.management.entity.Supplier;
import com.inventory.management.entity.User;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.repository.BrandRepository;
import com.inventory.management.repository.CategoryRepository;
import com.inventory.management.repository.CustomerRepository;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.RoleRepository;
import com.inventory.management.repository.SupplierRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.repository.WarehouseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class RealMySqlPhase3MasterDataVerificationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String runId;

    // Track IDs created during test to safely isolate cleanup without affecting Phase 2 data
    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdCategoryIds = new ArrayList<>();
    private final List<Long> createdBrandIds = new ArrayList<>();
    private final List<Long> createdSupplierIds = new ArrayList<>();
    private final List<Long> createdCustomerIds = new ArrayList<>();
    private final List<Long> createdWarehouseIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        runId = String.valueOf(System.currentTimeMillis() % 1000000);

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));

        userRepository.findByUsername("mysql-p3-admin")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("mysql-p3-admin")
                        .email("admin@p3mysql.test")
                        .passwordHash(passwordEncoder.encode("AdminPass-123"))
                        .role(adminRole)
                        .build()));

        MvcResult res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", "mysql-p3-admin", "password", "AdminPass-123"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = mapper.readTree(res.getResponse().getContentAsString());
        adminToken = node.path("data").path("accessToken").asText();
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdProductIds) {
            try { productRepository.deleteById(id); } catch (Exception ignored) {}
        }
        for (Long id : createdWarehouseIds) {
            try { warehouseRepository.deleteById(id); } catch (Exception ignored) {}
        }
        for (Long id : createdCustomerIds) {
            try { customerRepository.deleteById(id); } catch (Exception ignored) {}
        }
        for (Long id : createdSupplierIds) {
            try { supplierRepository.deleteById(id); } catch (Exception ignored) {}
        }
        for (Long id : createdBrandIds) {
            try { brandRepository.deleteById(id); } catch (Exception ignored) {}
        }
        for (Long id : createdCategoryIds) {
            try { categoryRepository.deleteById(id); } catch (Exception ignored) {}
        }
    }

    // ==========================================
    // 1. MYSQL VERSION VERIFICATION
    // ==========================================

    @Test
    @DisplayName("Verify MySQL server version is 8.x via SELECT VERSION()")
    void testMySqlVersion() {
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        System.out.println("==================================================");
        System.out.println("LIVE MYSQL SERVER VERSION: " + version);
        System.out.println("==================================================");
        assertThat(version).isNotNull();
        assertThat(version).startsWith("8.");
    }

    // ==========================================
    // 2. CATEGORY VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Category create, read, update, uniqueness, persistence")
    void testCategoryPersistence() throws Exception {
        String catName = "P3-Cat-" + runId;
        CategoryRequest req = new CategoryRequest();
        req.setName(catName);
        req.setDescription("P3 Category description");

        MvcResult res = mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(catName))
                .andReturn();

        Long id = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        createdCategoryIds.add(id);

        // Read from MySQL directly
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM categories WHERE id = ?", id);
        assertThat(row.get("name")).isEqualTo(catName);

        // Update
        req.setDescription("Updated P3 Description");
        mvc.perform(put("/categories/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Duplicate rejection
        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 3. BRAND VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Brand create, read, update, uniqueness, persistence")
    void testBrandPersistence() throws Exception {
        String brandName = "P3-Brand-" + runId;
        BrandRequest req = new BrandRequest();
        req.setName(brandName);
        req.setDescription("P3 Brand description");

        MvcResult res = mvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(brandName))
                .andReturn();

        Long id = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        createdBrandIds.add(id);

        // Read from MySQL
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM brands WHERE id = ?", id);
        assertThat(row.get("name")).isEqualTo(brandName);

        // Update
        req.setDescription("Updated Brand Description");
        mvc.perform(put("/brands/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Duplicate rejection
        mvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 4. SUPPLIER VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Supplier create, read, update, persistence")
    void testSupplierPersistence() throws Exception {
        String supplierName = "P3-Supplier-" + runId;
        SupplierRequest req = new SupplierRequest();
        req.setName(supplierName);
        req.setContactPerson("John Doe");
        req.setEmail("john@p3supplier.test");
        req.setPhone("+1-555-1234");
        req.setAddress("Industrial Area, Block B");

        MvcResult res = mvc.perform(post("/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(supplierName))
                .andReturn();

        Long id = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        createdSupplierIds.add(id);

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM suppliers WHERE id = ?", id);
        assertThat(row.get("name")).isEqualTo(supplierName);
        assertThat(row.get("contact_person")).isEqualTo("John Doe");

        // Update
        req.setContactPerson("Jane Smith");
        mvc.perform(put("/suppliers/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 5. CUSTOMER VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Customer create, read, update, persistence")
    void testCustomerPersistence() throws Exception {
        String customerName = "P3-Customer-" + runId;
        CustomerRequest req = new CustomerRequest();
        req.setName(customerName);
        req.setEmail("orders@p3customer.test");
        req.setPhone("+1-555-5678");
        req.setAddress("Commercial Boulevard 101");

        MvcResult res = mvc.perform(post("/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(customerName))
                .andReturn();

        Long id = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        createdCustomerIds.add(id);

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM customers WHERE id = ?", id);
        assertThat(row.get("name")).isEqualTo(customerName);

        // Update
        req.setAddress("Commercial Boulevard 102");
        mvc.perform(put("/customers/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 6. WAREHOUSE VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Warehouse create, read, update, persistence")
    void testWarehousePersistence() throws Exception {
        String whName = "P3-Wh-" + runId;
        WarehouseRequest req = new WarehouseRequest();
        req.setName(whName);
        req.setLocation("Warehouse Complex North");

        MvcResult res = mvc.perform(post("/warehouses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(whName))
                .andReturn();

        Long id = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        createdWarehouseIds.add(id);

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM warehouses WHERE id = ?", id);
        assertThat(row.get("name")).isEqualTo(whName);

        // Update
        req.setLocation("Warehouse Complex North Wing");
        mvc.perform(put("/warehouses/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 7. PRODUCT & RELATIONSHIPS VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Product create with Category & Brand, read, update, category_id/brand_id persistence, 404s, uniqueness")
    void testProductPersistenceAndRelationships() throws Exception {
        Category cat = categoryRepository.save(Category.builder().name("P3-ProdCat-" + runId).description("Desc").build());
        createdCategoryIds.add(cat.getId());

        Brand brd = brandRepository.save(Brand.builder().name("P3-ProdBrd-" + runId).description("Desc").build());
        createdBrandIds.add(brd.getId());

        String sku = "P3-SKU-" + runId;
        ProductRequest req = new ProductRequest();
        req.setSku(sku);
        req.setName("P3 Verified Product");
        req.setDescription("Verified against MySQL");
        req.setCategoryId(cat.getId());
        req.setBrandId(brd.getId());
        req.setBasePrice(new BigDecimal("199.99"));

        // 1. Create with Category + Brand
        MvcResult res = mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value(sku))
                .andExpect(jsonPath("$.data.categoryId").value(cat.getId()))
                .andExpect(jsonPath("$.data.categoryName").value(cat.getName()))
                .andExpect(jsonPath("$.data.brandId").value(brd.getId()))
                .andExpect(jsonPath("$.data.brandName").value(brd.getName()))
                .andReturn();

        Long prodId = mapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        createdProductIds.add(prodId);

        // 2. Direct MySQL verification of category_id and brand_id columns
        Map<String, Object> productRow = jdbcTemplate.queryForMap(
                "SELECT id, sku, name, category_id, brand_id, base_price FROM products WHERE id = ?", prodId);
        assertThat(((Number) productRow.get("category_id")).longValue()).isEqualTo(cat.getId());
        assertThat(((Number) productRow.get("brand_id")).longValue()).isEqualTo(brd.getId());
        assertThat(productRow.get("sku")).isEqualTo(sku);

        // 3. Read via API
        mvc.perform(get("/products/" + prodId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value(cat.getId()))
                .andExpect(jsonPath("$.data.brandId").value(brd.getId()));

        // 4. Update Product
        req.setName("P3 Updated Product");
        mvc.perform(put("/products/" + prodId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("P3 Updated Product"));

        // 5. Invalid Category -> 404
        req.setSku("P3-INV-CAT-" + runId);
        req.setCategoryId(99999999L);
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        // 6. Invalid Brand -> 404
        req.setSku("P3-INV-BRD-" + runId);
        req.setCategoryId(cat.getId());
        req.setBrandId(99999999L);
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        // 7. Duplicate SKU -> 400
        req.setSku(sku);
        req.setBrandId(brd.getId());
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 8. DATABASE SCHEMA & FOREIGN KEYS VERIFICATION
    // ==========================================

    @Test
    @DisplayName("MySQL: Verify foreign key constraints fk_products_category, fk_products_brand and NOT NULL columns")
    void testDatabaseConstraintsInMySql() {
        // Verify NOT NULL on category_id and brand_id
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, IS_NULLABLE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'stock_management' AND TABLE_NAME = 'products' " +
                "AND COLUMN_NAME IN ('category_id', 'brand_id')");

        assertThat(columns).hasSize(2);
        for (Map<String, Object> col : columns) {
            assertThat(col.get("IS_NULLABLE")).isEqualTo("NO");
        }

        // Verify foreign key constraints in information_schema
        List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                "FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = 'stock_management' AND TABLE_NAME = 'products' " +
                "AND CONSTRAINT_NAME IN ('fk_products_category', 'fk_products_brand')");

        assertThat(fks).hasSize(2);

        Map<String, Object> catFk = fks.stream()
                .filter(fk -> "fk_products_category".equals(fk.get("CONSTRAINT_NAME")))
                .findFirst().orElseThrow();
        assertThat(catFk.get("COLUMN_NAME")).isEqualTo("category_id");
        assertThat(catFk.get("REFERENCED_TABLE_NAME")).isEqualTo("categories");
        assertThat(catFk.get("REFERENCED_COLUMN_NAME")).isEqualTo("id");

        Map<String, Object> brdFk = fks.stream()
                .filter(fk -> "fk_products_brand".equals(fk.get("CONSTRAINT_NAME")))
                .findFirst().orElseThrow();
        assertThat(brdFk.get("COLUMN_NAME")).isEqualTo("brand_id");
        assertThat(brdFk.get("REFERENCED_TABLE_NAME")).isEqualTo("brands");
        assertThat(brdFk.get("REFERENCED_COLUMN_NAME")).isEqualTo("id");
    }
}

