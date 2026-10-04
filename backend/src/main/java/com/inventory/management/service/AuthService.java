package com.inventory.management.service;

import com.inventory.management.dto.request.LoginRequest;
import com.inventory.management.dto.request.RegisterRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    ApiResponse<?> login(LoginRequest request);
    ApiResponse<?> register(RegisterRequest request);
    ApiResponse<?> refreshToken(String refreshToken);
    ApiResponse<?> logout(String authorizationHeader, String refreshToken);
    ApiResponse<?> currentUser(Authentication authentication);
}
