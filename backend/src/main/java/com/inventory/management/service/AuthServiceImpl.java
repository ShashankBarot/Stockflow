package com.inventory.management.service;

import com.inventory.management.dto.request.LoginRequest;
import com.inventory.management.dto.request.RegisterRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.AuthResponse;
import com.inventory.management.dto.response.UserResponse;
import com.inventory.management.entity.Role;
import com.inventory.management.entity.User;
import com.inventory.management.repository.RoleRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;
    private final AuthTokenService tokens;

    @Override
    @Transactional
    public ApiResponse<?> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = users.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.ok("Signed in", issueTokens(user));
    }

    @Override
    @Transactional
    public ApiResponse<?> register(RegisterRequest request) {
        if (users.existsByUsername(request.getUsername())) throw new IllegalArgumentException("Username is already in use");
        if (users.existsByEmail(request.getEmail())) throw new IllegalArgumentException("Email is already in use");
        Role role = roles.findByName("STAFF").orElseThrow(() -> new IllegalStateException("Default STAFF role is missing"));
        User user = users.save(User.builder().username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .passwordHash(passwordEncoder.encode(request.getPassword())).role(role).build());
        return ApiResponse.ok("Account created", UserResponse.from(user));
    }

    @Override
    @Transactional
    public ApiResponse<?> refreshToken(String refreshToken) {
        User user = tokens.consumeRefreshToken(refreshToken);
        return ApiResponse.ok("Token refreshed", issueTokens(user));
    }

    @Override
    @Transactional
    public ApiResponse<?> logout(String authorizationHeader, String refreshToken) {
        tokens.revoke(refreshToken);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String username = jwt.getUsernameFromToken(authorizationHeader.substring(7));
            users.findByUsername(username).ifPresent(tokens::revokeForUser);
        }
        return ApiResponse.ok("Signed out", null);
    }

    @Override
    public ApiResponse<?> currentUser(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User user = users.findByUsername(principal.getUsername()).orElseThrow();
        return ApiResponse.ok(UserResponse.from(user));
    }

    private AuthResponse issueTokens(User user) {
        String role = user.getRole().getName();
        return new AuthResponse(jwt.generateAccessToken(user.getId(), user.getUsername(), role),
                tokens.issueRefreshToken(user), UserResponse.from(user));
    }
}
