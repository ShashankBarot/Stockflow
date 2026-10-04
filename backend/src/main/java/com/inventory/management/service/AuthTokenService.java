package com.inventory.management.service;

import com.inventory.management.entity.RefreshToken;
import com.inventory.management.entity.User;
import com.inventory.management.repository.RefreshTokenRepository;
import com.inventory.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    private final JwtTokenProvider jwt;
    private final RefreshTokenRepository refreshTokens;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @Transactional
    public String issueRefreshToken(User user) {
        String token = jwt.generateRefreshToken(user.getId(), user.getUsername());
        refreshTokens.save(RefreshToken.builder().user(user).tokenHash(hash(token))
                .expiresAt(Instant.now().plusMillis(refreshExpiration)).build());
        return token;
    }

    @Transactional
    public User consumeRefreshToken(String token) {
        if (token == null || !jwt.isRefreshToken(token)) throw new IllegalArgumentException("Invalid refresh token");
        RefreshToken stored = refreshTokens.findByTokenHashAndRevokedAtIsNull(hash(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now()) || !jwt.validateToken(token)) {
            stored.setRevokedAt(Instant.now());
            throw new IllegalArgumentException("Expired refresh token");
        }
        stored.setRevokedAt(Instant.now());
        return stored.getUser();
    }

    @Transactional
    public void revokeForUser(User user) {
        refreshTokens.deleteAllByUserId(user.getId());
    }

    public void revoke(String token) {
        if (token == null || !jwt.validateToken(token) || !jwt.isRefreshToken(token)) return;
        refreshTokens.findByTokenHashAndRevokedAtIsNull(hash(token)).ifPresent(t -> t.setRevokedAt(Instant.now()));
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
