package com.inventory.management.service;

import com.inventory.management.dto.response.AnalyticsSummaryResponse;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.LowStockAlertResponse;
import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.StockMovement;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final InventoryRepository inventoryRepository;
    private final MovementRepository movementRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getSummary() {
        BigDecimal totalValue = inventoryRepository.calculateTotalStockValue();
        if (totalValue == null) {
            totalValue = BigDecimal.ZERO;
        }

        List<Inventory> lowStockItems = inventoryRepository.findLowStock();
        long lowStockCount = lowStockItems.size();

        Page<StockMovement> recentMovements = movementRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Map<String, Object>> recentTransactions = recentMovements.stream().map(movement -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", movement.getId());
            map.put("type", movement.getType().name());
            map.put("productId", movement.getProduct().getId());
            map.put("productName", movement.getProduct().getName());
            map.put("quantity", movement.getQuantity());
            map.put("createdAt", movement.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        AnalyticsSummaryResponse summary = AnalyticsSummaryResponse.builder()
                .totalStockValue(totalValue)
                .lowStockAlertCount(lowStockCount)
                .recentTransactions(recentTransactions)
                .build();

        return ApiResponse.ok(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getLowStockAlerts() {
        List<Inventory> lowStockItems = inventoryRepository.findLowStock();
        List<LowStockAlertResponse> alerts = lowStockItems.stream().map(inv -> LowStockAlertResponse.builder()
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .warehouseId(inv.getWarehouse().getId())
                .warehouseName(inv.getWarehouse().getName())
                .quantity(inv.getQuantity())
                .minThreshold(inv.getMinThreshold())
                .build()
        ).collect(Collectors.toList());

        return ApiResponse.ok(alerts);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getRecentTransactions(Pageable pageable) {
        Page<StockMovement> movements = movementRepository.findAll(pageable);
        Page<Map<String, Object>> transactions = movements.map(movement -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", movement.getId());
            map.put("type", movement.getType().name());
            map.put("productId", movement.getProduct().getId());
            map.put("productName", movement.getProduct().getName());
            map.put("quantity", movement.getQuantity());
            map.put("warehouseId", movement.getWarehouse().getId());
            map.put("warehouseName", movement.getWarehouse().getName());
            map.put("createdAt", movement.getCreatedAt());
            return map;
        });
        return ApiResponse.ok(transactions);
    }
}
