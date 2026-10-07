package com.inventory.management.service;

import com.inventory.management.dto.request.ProductRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.ProductResponse;
import com.inventory.management.entity.Brand;
import com.inventory.management.entity.Category;
import com.inventory.management.entity.Product;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.BrandRepository;
import com.inventory.management.repository.CategoryRepository;
import com.inventory.management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BrandRepository brands;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllProducts(Pageable pageable) {
        return ApiResponse.ok(products.findAll(pageable).map(ProductResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getProductById(Long id) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ApiResponse.ok(ProductResponse.from(product));
    }

    @Override
    @Transactional
    public ApiResponse<?> createProduct(ProductRequest request) {
        if (products.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categories.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brands.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        Product product = products.save(Product.builder()
                .sku(request.getSku().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(category)
                .brand(brand)
                .basePrice(request.getBasePrice())
                .build());
        return ApiResponse.ok("Product created", ProductResponse.from(product));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateProduct(Long id, ProductRequest request) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (!product.getSku().equals(request.getSku()) && products.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categories.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brands.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        product.setSku(request.getSku().trim());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        product.setBasePrice(request.getBasePrice());
        return ApiResponse.ok("Product updated", ProductResponse.from(products.save(product)));
    }
}

