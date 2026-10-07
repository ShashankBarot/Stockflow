package com.inventory.management.service;

import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface AnalyticsService {
    ApiResponse<?> getSummary();
    ApiResponse<?> getLowStockAlerts();
    ApiResponse<?> getRecentTransactions(Pageable pageable);
}
