package com.inventory.management.service;

import com.inventory.management.dto.request.CustomerRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.CustomerResponse;
import com.inventory.management.entity.Customer;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customers;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllCustomers(Pageable pageable) {
        return ApiResponse.ok(customers.findAll(pageable).map(CustomerResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCustomerById(Long id) {
        Customer customer = customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return ApiResponse.ok(CustomerResponse.from(customer));
    }

    @Override
    @Transactional
    public ApiResponse<?> createCustomer(CustomerRequest request) {
        if (customers.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Customer with email '" + request.getEmail() + "' already exists");
        }
        Customer customer = customers.save(Customer.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());
        return ApiResponse.ok("Customer created", CustomerResponse.from(customer));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        if (!customer.getEmail().equals(request.getEmail()) && customers.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Customer with email '" + request.getEmail() + "' already exists");
        }
        customer.setName(request.getName().trim());
        customer.setEmail(request.getEmail().trim());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        return ApiResponse.ok("Customer updated", CustomerResponse.from(customers.save(customer)));
    }
}

