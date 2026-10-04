package com.inventory.management.controller;

import com.inventory.management.dto.request.LoginRequest;
import com.inventory.management.dto.request.RegisterRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.refreshToken(request.get("refreshToken")));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseEntity.ok(authService.logout(token));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMe() {
        // TODO: Implement
        return ResponseEntity.ok(ApiResponse.ok("Not implemented", null));
    }
}
