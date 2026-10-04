package com.inventory.management.config;

import com.inventory.management.entity.Role;
import com.inventory.management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoleDataInitializer {
    private final RoleRepository roles;

    @Bean
    CommandLineRunner seedRoles() {
        return args -> {
            for (String name : new String[]{"ADMIN", "MANAGER", "STAFF"}) {
                if (roles.findByName(name).isEmpty()) roles.save(Role.builder().name(name).build());
            }
        };
    }
}
