package com.inventory.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfer_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @OneToMany(mappedBy = "transferOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransferOrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transferDate == null) {
            transferDate = LocalDateTime.now();
        }
        if (status == null) {
            status = TransferStatus.PENDING;
        }
    }

    public enum TransferStatus {
        PENDING, COMPLETED, CANCELLED
    }
}

