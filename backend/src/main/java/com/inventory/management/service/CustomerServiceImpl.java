package com.inventory.management.service;

import com.inventory.management.dto.request.CustomerRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.CustomerResponse;
import com.inventory.management.entity.Customer;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllCustomers(String search, Pageable pageable) {
        Page<Customer> page = (search != null && !search.isBlank())
                ? customerRepository.findByNameContainingIgnoreCase(search.trim(), pageable)
                : customerRepository.findAll(pageable);
        return ApiResponse.ok(page.map(CustomerResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return ApiResponse.ok(CustomerResponse.from(customer));
    }

    @Override
    @Transactional
    public ApiResponse<?> createCustomer(CustomerRequest request) {
        Customer customer = customerRepository.save(Customer.builder()
                .name(request.getName().trim())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());
        return ApiResponse.ok("Customer created", CustomerResponse.from(customer));
    }

    @Override
    @Transactional
    public ApiResponse<?> updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setName(request.getName().trim());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        return ApiResponse.ok("Customer updated", CustomerResponse.from(customerRepository.save(customer)));
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(customer);
        return ApiResponse.ok("Customer deleted", null);
    }
}

