package com.inventory.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., "CREATE_PRODUCT", "UPDATE_STOCK"

    @Column(nullable = false, name = "entity_name")
    private String entityName; // e.g., "Product", "PurchaseOrder"

    @Column(nullable = false, name = "entity_id")
    private String entityId;

    @Column(name = "user_id")
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime timestamp;
}
