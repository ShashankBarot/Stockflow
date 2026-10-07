package com.inventory.management.service;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.NotificationResponse;
import com.inventory.management.entity.Notification;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotification(String title, String message, Notification.NotificationType type, Long userId) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .userId(userId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getUserNotifications(Long userId, Pageable pageable) {
        return ApiResponse.ok(notificationRepository.findByUserId(userId, pageable).map(NotificationResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getUnreadNotifications(Long userId, Pageable pageable) {
        return ApiResponse.ok(notificationRepository.findByUserIdAndIsReadFalse(userId, pageable).map(NotificationResponse::from));
    }

    @Override
    @Transactional
    public ApiResponse<?> markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
        return ApiResponse.ok("Notification marked as read", NotificationResponse.from(notification));
    }
}
