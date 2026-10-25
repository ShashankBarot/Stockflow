package com.inventory.management.controller;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.NotificationService;
import com.inventory.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getUserNotifications(
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(authService.userId(authentication.getName()), pageable));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<ApiResponse<?>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getUnreadCount(authService.userId(authentication.getName())));
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(Authentication authentication) {
        return ResponseEntity.ok(notificationService.markAllAsRead(authService.userId(authentication.getName())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(notificationService.markAsRead(id, authService.userId(authentication.getName())));
    }

    @GetMapping("/me/unread")
    public ResponseEntity<ApiResponse<?>> getUnreadNotifications(
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(authService.userId(authentication.getName()), pageable));
    }
}
