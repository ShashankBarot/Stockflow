package com.inventory.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.inventory.management.entity.Role;
import com.inventory.management.entity.User;
import com.inventory.management.repository.RoleRepository;
import com.inventory.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-security;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=stockflow-auth-integration-test-secret-key-32chars",
        "app.jwt.access-token-expiration=900000",
        "app.jwt.refresh-token-expiration=604800000",
        "JWT_SECRET=stockflow-auth-integration-test-secret-key-32chars",
        "JWT_ACCESS_EXPIRATION=900000",
        "JWT_REFRESH_EXPIRATION=604800000",
        "app.bootstrap-admin.username=auth-admin",
        "app.bootstrap-admin.email=auth-admin@example.test",
        "app.bootstrap-admin.password=ChangeMe-Integration-123"
})
class AuthSecurityIntegrationTest {
    private static final String BASE = "";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired RoleRepository roles;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthenticationManager authenticationManager;

    @BeforeEach
    void seedAuthenticationRecords() {
        roles.findByName("ADMIN").orElseGet(() -> roles.save(Role.builder().name("ADMIN").build()));
        roles.findByName("MANAGER").orElseGet(() -> roles.save(Role.builder().name("MANAGER").build()));
        roles.findByName("STAFF").orElseGet(() -> roles.save(Role.builder().name("STAFF").build()));
        User admin = users.findByUsername("auth-admin").orElseGet(User::new);
        admin.setUsername("auth-admin");
        admin.setEmail("auth-admin@example.test");
        admin.setPasswordHash(passwordEncoder.encode("ChangeMe-Integration-123"));
        admin.setRole(roles.findByName("ADMIN").orElseThrow());
        users.save(admin);
    }

    @Test
    void diagnosticAdminCredentialsAuthenticate() {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("auth-admin", "ChangeMe-Integration-123"));
        assertThat(authentication.isAuthenticated()).isTrue();
    }

    @Test
    void authLifecycleEnforcesIdentityAndRotatesRefreshTokens() throws Exception {
        mvc.perform(get(BASE + "/auth/me")).andExpect(status().isUnauthorized());

        JsonNode initial = login("auth-admin", "ChangeMe-Integration-123");
        String accessToken = initial.path("data").path("accessToken").asText();
        String refreshToken = initial.path("data").path("refreshToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        MvcResult current = mvc.perform(get(BASE + "/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode currentUser = mapper.readTree(current.getResponse().getContentAsString()).path("data");
        assertThat(currentUser.path("username").asText()).isEqualTo("auth-admin");
        assertThat(currentUser.path("role").asText()).isEqualTo("ADMIN");

        String rotatedRefresh = refresh(refreshToken);
        assertThat(rotatedRefresh).isNotEqualTo(refreshToken);
        mvc.perform(post(BASE + "/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());

        mvc.perform(post(BASE + "/auth/logout").header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("refreshToken", rotatedRefresh))))
                .andExpect(status().isOk());
        mvc.perform(post(BASE + "/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("refreshToken", rotatedRefresh))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationAndDeferredBusinessRoutesEnforceRoles() throws Exception {
        mvc.perform(post(BASE + "/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());

        JsonNode admin = login("auth-admin", "ChangeMe-Integration-123");
        String adminAccess = admin.path("data").path("accessToken").asText();
        mvc.perform(post(BASE + "/auth/register").header("Authorization", "Bearer " + adminAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"auth-staff\",\"email\":\"auth-staff@example.test\",\"password\":\"StaffPass-123\"}"))
                .andExpect(status().isOk());

        JsonNode staff = login("auth-staff", "StaffPass-123");
        String staffAccess = staff.path("data").path("accessToken").asText();
        assertThat(staff.path("data").path("user").path("role").asText()).isEqualTo("STAFF");

        mvc.perform(post(BASE + "/auth/register").header("Authorization", "Bearer " + staffAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"another-staff\",\"email\":\"another@example.test\",\"password\":\"StaffPass-123\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(BASE + "/products").header("Authorization", "Bearer " + staffAccess)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    private JsonNode login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post(BASE + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private String refresh(String refreshToken) throws Exception {
        MvcResult result = mvc.perform(post(BASE + "/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).path("data").path("refreshToken").asText();
    }
}
