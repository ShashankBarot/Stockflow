package com.inventory.management.controller;

import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getProductById(
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(
            @RequestBody Object request) {
        return ResponseEntity.ok(inventoryService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable Long id,
            @RequestBody Object request) {
        return ResponseEntity.ok(inventoryService.updateProduct(id, request));
    }
}
