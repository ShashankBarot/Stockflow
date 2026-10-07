package com.inventory.management.dto.response;

import com.inventory.management.entity.AuditLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entityName;
    private String entityId;
    private Long userId;
    private String details;
    private LocalDateTime timestamp;

    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .userId(log.getUserId())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
