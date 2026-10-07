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
public class SaleOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String referenceNumber;
    private String notes;

    @NotEmpty(message = "Order must have at least one item")
    private List<SaleOrderItemRequest> items;
}

