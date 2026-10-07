package com.inventory.management.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String referenceNumber;
    private String notes;

    @NotEmpty(message = "Order must have at least one item")
    private List<PurchaseOrderItemRequest> items;
}

