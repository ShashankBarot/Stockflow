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
public class DispatchOrderRequest {

    @NotNull(message = "Source warehouse ID is required")
    private Long warehouseId;
}

