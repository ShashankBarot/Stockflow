package com.inventory.management.service;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.entity.Notification.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void createNotification(String title, String message, NotificationType type, Long userId);
    ApiResponse<?> getUserNotifications(Long userId, Pageable pageable);
    ApiResponse<?> getUnreadNotifications(Long userId, Pageable pageable);
    ApiResponse<?> markAsRead(Long notificationId);
}
