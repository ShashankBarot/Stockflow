package com.inventory.management.config;

import com.inventory.management.entity.Role;
import com.inventory.management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import com.inventory.management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import com.inventory.management.entity.User;

@Configuration
@RequiredArgsConstructor
public class RoleDataInitializer {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.bootstrap-admin.username:}") private String adminUsername;
    @Value("${app.bootstrap-admin.email:}") private String adminEmail;
    @Value("${app.bootstrap-admin.password:}") private String adminPassword;

    @Bean
    @DependsOn("entityManagerFactory")
    public CommandLineRunner seedRoles() {
        return args -> {
            for (String name : new String[]{"ADMIN", "MANAGER", "STAFF"}) {
                if (roles.findByName(name).isEmpty()) roles.save(Role.builder().name(name).build());
            }
            Role staff = roles.findByName("STAFF").orElseThrow();
            for (User existing : users.findAll()) {
                if (existing.getRole() == null || !java.util.Set.of("ADMIN", "MANAGER", "STAFF").contains(existing.getRole().getName())) {
                    existing.setRole(staff);
                    users.save(existing);
                }
            }
            if (!adminUsername.isBlank() && !adminEmail.isBlank() && !adminPassword.isBlank()
                    && users.count() == 0) {
                Role admin = roles.findByName("ADMIN").orElseThrow();
                users.save(User.builder().username(adminUsername.trim()).email(adminEmail.trim().toLowerCase())
                        .passwordHash(passwordEncoder.encode(adminPassword)).role(admin).build());
            }
        };
    }
}
