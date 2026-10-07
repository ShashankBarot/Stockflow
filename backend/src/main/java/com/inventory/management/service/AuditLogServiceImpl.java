package com.inventory.management.service;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.AuditLogResponse;
import com.inventory.management.entity.AuditLog;
import com.inventory.management.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(String action, String entityName, String entityId, Long userId, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .userId(userId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getLogs(Pageable pageable) {
        return ApiResponse.ok(auditLogRepository.findAll(pageable).map(AuditLogResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getLogsByUser(Long userId, Pageable pageable) {
        return ApiResponse.ok(auditLogRepository.findByUserId(userId, pageable).map(AuditLogResponse::from));
    }
}
