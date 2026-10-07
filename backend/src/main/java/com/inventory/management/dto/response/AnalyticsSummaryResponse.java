package com.inventory.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsSummaryResponse {
    private BigDecimal totalStockValue;
    private long lowStockAlertCount;
    private List<Map<String, Object>> recentTransactions;
}
