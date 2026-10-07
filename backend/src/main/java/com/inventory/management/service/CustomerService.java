package com.inventory.management.service;

import com.inventory.management.dto.request.CustomerRequest;
import com.inventory.management.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    ApiResponse<?> getAllCustomers(String search, Pageable pageable);

    default ApiResponse<?> getAllCustomers(Pageable pageable) {
        return getAllCustomers(null, pageable);
    }

    ApiResponse<?> getCustomerById(Long id);

    ApiResponse<?> createCustomer(CustomerRequest request);

    ApiResponse<?> updateCustomer(Long id, CustomerRequest request);

    ApiResponse<?> deleteCustomer(Long id);
}

