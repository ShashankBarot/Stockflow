package com.inventory.management.service;

import com.inventory.management.dto.request.ProductRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ApiResponse<?> getAllProducts(Pageable pageable);

    ApiResponse<?> getProductById(Long id);

    ApiResponse<?> createProduct(ProductRequest request);

    ApiResponse<?> updateProduct(Long id, ProductRequest request);
}

