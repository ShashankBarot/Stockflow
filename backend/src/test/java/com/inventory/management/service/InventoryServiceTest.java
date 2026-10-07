package com.inventory.management.service;

import com.inventory.management.dto.request.InventoryThresholdRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.InventoryResponse;
import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;
    private Warehouse testWarehouse;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        com.inventory.management.entity.Category category = com.inventory.management.entity.Category.builder().id(10L).name("General").build();
        com.inventory.management.entity.Brand brand = com.inventory.management.entity.Brand.builder().id(20L).name("Generic").build();

        testProduct = Product.builder()
                .id(1L)
                .sku("INV-SKU-1")
                .name("Inventory Test Item")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("10.00"))
                .build();

        testWarehouse = Warehouse.builder()
                .id(2L)
                .name("Inventory Test Depot")
                .location("Aisle 1")
                .build();

        testInventory = Inventory.builder()
                .id(5L)
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(50)
                .minThreshold(10)
                .maxCapacity(100)
                .build();
    }

    @Test
    void getInventoryReturnsAllPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> page = new PageImpl<>(List.of(testInventory), pageable, 1);
        when(inventoryRepository.findAll(pageable)).thenReturn(page);

        ApiResponse<?> response = inventoryService.getInventory(null, null, null, pageable);

        assertThat(response.isSuccess()).isTrue();
        verify(inventoryRepository).findAll(pageable);
    }

    @Test
    void getInventoryFiltersByWarehouse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> page = new PageImpl<>(List.of(testInventory), pageable, 1);
        when(inventoryRepository.findByWarehouseId(2L, pageable)).thenReturn(page);

        ApiResponse<?> response = inventoryService.getInventory(null, 2L, null, pageable);

        assertThat(response.isSuccess()).isTrue();
        verify(inventoryRepository).findByWarehouseId(2L, pageable);
    }

    @Test
    void getInventoryFiltersByLowStock() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> page = new PageImpl<>(List.of(testInventory), pageable, 1);
        when(inventoryRepository.findLowStock(pageable)).thenReturn(page);

        ApiResponse<?> response = inventoryService.getInventory(null, null, true, pageable);

        assertThat(response.isSuccess()).isTrue();
        verify(inventoryRepository).findLowStock(pageable);
    }

    @Test
    void getInventoryByWarehouseThrowsIfWarehouseDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(warehouseRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.getInventoryByWarehouse(999L, pageable));
    }

    @Test
    void getInventoryByIdReturnsRecordOrThrows() {
        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(testInventory));

        ApiResponse<?> res = inventoryService.getInventoryById(5L);
        assertThat(res.isSuccess()).isTrue();

        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventoryById(999L));
    }

    @Test
    void updateInventoryThresholdUpdatesThresholdOnlyWithoutModifyingQuantity() {
        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryThresholdRequest req = new InventoryThresholdRequest(25, 150);

        ApiResponse<?> response = inventoryService.updateInventoryThreshold(5L, req);

        assertThat(response.isSuccess()).isTrue();
        InventoryResponse data = (InventoryResponse) response.getData();
        assertThat(data.minThreshold()).isEqualTo(25);
        assertThat(data.maxCapacity()).isEqualTo(150);
        assertThat(data.quantity()).isEqualTo(50); // Quantity untouched!
        assertThat(testInventory.getQuantity()).isEqualTo(50);

        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void updateInventoryThresholdRejectsNegativeThreshold() {
        InventoryThresholdRequest req = new InventoryThresholdRequest(-5, 100);
        assertThrows(IllegalArgumentException.class, () -> inventoryService.updateInventoryThreshold(5L, req));

        InventoryThresholdRequest nullReq = new InventoryThresholdRequest(null, 100);
        assertThrows(IllegalArgumentException.class, () -> inventoryService.updateInventoryThreshold(5L, nullReq));
    }
}

