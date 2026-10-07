package com.inventory.management.service;

import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void logAction(String action, String entityName, String entityId, Long userId, String details);
    ApiResponse<?> getLogs(Pageable pageable);
    ApiResponse<?> getLogsByUser(Long userId, Pageable pageable);
}
