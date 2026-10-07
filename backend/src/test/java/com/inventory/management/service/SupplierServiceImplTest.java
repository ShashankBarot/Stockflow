package com.inventory.management.service;

import com.inventory.management.dto.request.SupplierRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.SupplierResponse;
import com.inventory.management.entity.Supplier;
import com.inventory.management.repository.SupplierRepository;
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
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .id(1L)
                .name("Acme Corp")
                .contactEmail("contact@acme.test")
                .build();
    }

    @Test
    void getAllSuppliers() {
        when(supplierRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(supplier)));

        ApiResponse<?> response = supplierService.getAllSuppliers(PageRequest.of(0, 10));
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isInstanceOf(Page.class);
    }

    @Test
    void getSupplierById() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        ApiResponse<?> response = supplierService.getSupplierById(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((SupplierResponse) response.getData()).name()).isEqualTo("Acme Corp");
    }

    @Test
    void createSupplier() {
        when(supplierRepository.existsByName("Acme Corp")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        SupplierRequest request = new SupplierRequest("Acme Corp", "contact@acme.test", "12345", "Address");
        ApiResponse<?> response = supplierService.createSupplier(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(((SupplierResponse) response.getData()).name()).isEqualTo("Acme Corp");
    }
}
