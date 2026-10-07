package com.inventory.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(min = 1, max = 50, message = "SKU must be between 1 and 50 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    private String description;

    private Long categoryId;

    private Long brandId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
    private BigDecimal basePrice;
}

