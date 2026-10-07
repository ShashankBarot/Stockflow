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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository products;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllProducts(String search, String category, String brand, Pageable pageable) {
        boolean hasFilter = (search != null && !search.isBlank())
                || (category != null && !category.isBlank())
                || (brand != null && !brand.isBlank());

        Page<Product> page = hasFilter
                ? products.searchProducts(
                        search != null && !search.isBlank() ? search.trim() : null,
                        category != null && !category.isBlank() ? category.trim() : null,
                        brand != null && !brand.isBlank() ? brand.trim() : null,
                        pageable)
                : products.findAllWithRelations(pageable);

        return ApiResponse.ok(page.map(ProductResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getProductById(Long id) {
        Product product = products.findWithRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ApiResponse.ok(ProductResponse.from(product));
    }

    @Override
    @Transactional
    public ApiResponse<?> createProduct(ProductRequest request) {
        if (products.existsBySku(request.getSku().trim())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku().trim() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

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

        String trimmedSku = request.getSku().trim();
        if (!product.getSku().equals(trimmedSku) && products.existsBySku(trimmedSku)) {
            throw new IllegalArgumentException("Product with SKU '" + trimmedSku + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        product.setSku(trimmedSku);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        product.setBasePrice(request.getBasePrice());

        return ApiResponse.ok("Product updated", ProductResponse.from(products.save(product)));
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteProduct(Long id) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        products.delete(product);
        return ApiResponse.ok("Product deleted", null);
    }
}
