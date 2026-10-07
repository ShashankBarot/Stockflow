package com.inventory.management.service;

import com.inventory.management.dto.request.CustomerRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.CustomerResponse;
import com.inventory.management.entity.Customer;
import com.inventory.management.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.test")
                .build();
    }

    @Test
    void getAllCustomers() {
        when(customerRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(customer)));

        ApiResponse<?> response = customerService.getAllCustomers(PageRequest.of(0, 10));
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isInstanceOf(Page.class);
    }

    @Test
    void getCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        ApiResponse<?> response = customerService.getCustomerById(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((CustomerResponse) response.getData()).name()).isEqualTo("John Doe");
    }

    @Test
    void createCustomer() {
        when(customerRepository.existsByEmail("john@example.test")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerRequest request = new CustomerRequest("John Doe", "john@example.test", "12345", "Address");
        ApiResponse<?> response = customerService.createCustomer(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((CustomerResponse) response.getData()).name()).isEqualTo("John Doe");
    }
}

