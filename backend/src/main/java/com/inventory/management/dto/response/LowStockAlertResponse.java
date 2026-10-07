package com.inventory.management.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowStockAlertResponse {
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
    private Integer minThreshold;
}
