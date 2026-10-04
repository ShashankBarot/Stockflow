package com.inventory.management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        provider = new JwtTokenProvider();
        set("jwtSecret", "this-is-a-long-test-secret-key-that-is-at-least-32-bytes");
        set("accessTokenExpiration", 60_000L);
        set("refreshTokenExpiration", 120_000L);
    }

    private void set(String name, Object value) throws Exception {
        Field field = JwtTokenProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(provider, value);
    }

    @Test
    void differentiatesAccessAndRefreshTokens() {
        String access = provider.generateAccessToken(7L, "operator", "STAFF");
        String refresh = provider.generateRefreshToken(7L, "operator");

        assertTrue(provider.validateToken(access));
        assertTrue(provider.isAccessToken(access));
        assertFalse(provider.isRefreshToken(access));
        assertTrue(provider.validateToken(refresh));
        assertTrue(provider.isRefreshToken(refresh));
        assertFalse(provider.isAccessToken(refresh));
    }

    @Test
    void rejectsMalformedToken() {
        assertFalse(provider.validateToken("not-a-jwt"));
        assertFalse(provider.isRefreshToken("not-a-jwt"));
    }
}
