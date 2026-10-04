package com.inventory.management.controller;

import com.inventory.management.dto.request.LoginRequest;
import com.inventory.management.dto.request.RegisterRequest;
import com.inventory.management.dto.request.RefreshTokenRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader(value = "Authorization", required = false) String token,
                                                  @RequestBody(required = false) RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.logout(token, request == null ? null : request.getRefreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMe(Authentication authentication) {
        return ResponseEntity.ok(authService.currentUser(authentication));
    }
}
