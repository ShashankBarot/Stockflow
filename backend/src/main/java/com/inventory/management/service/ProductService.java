package com.inventory.management.service;

import com.inventory.management.dto.request.ProductRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    default ApiResponse<?> getAllProducts(Pageable pageable) {
        return getAllProducts(null, null, null, pageable);
    }

    default ApiResponse<?> getAllProducts(String search, String category, Pageable pageable) {
        return getAllProducts(search, category, null, pageable);
    }

    ApiResponse<?> getAllProducts(String search, String category, String brand, Pageable pageable);

    ApiResponse<?> getProductById(Long id);

    ApiResponse<?> createProduct(ProductRequest request);

    ApiResponse<?> updateProduct(Long id, ProductRequest request);

    ApiResponse<?> deleteProduct(Long id);
}
