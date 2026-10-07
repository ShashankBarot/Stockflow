package com.inventory.management.controller;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserNotifications(
            @PathVariable Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<?>> getUnreadNotifications(
            @PathVariable Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId, pageable));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}
