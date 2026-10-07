package com.inventory.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveOrderRequest {

    @NotNull(message = "Destination warehouse ID is required")
    private Long warehouseId;
}

