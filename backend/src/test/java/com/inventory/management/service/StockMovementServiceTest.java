package com.inventory.management.service;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.StockMovementResponse;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.entity.User;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.MovementRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    private Product testProduct;
    private Warehouse testWarehouse;
    private User testUser;
    private StockMovement testMovement;

    @BeforeEach
    void setUp() {
        com.inventory.management.entity.Category category = com.inventory.management.entity.Category.builder().id(10L).name("General").build();
        com.inventory.management.entity.Brand brand = com.inventory.management.entity.Brand.builder().id(20L).name("Generic").build();

        testProduct = Product.builder()
                .id(1L)
                .sku("SKU-SM-1")
                .name("Movement Test Prod")
                .category(category)
                .brand(brand)
                .basePrice(new BigDecimal("25.00"))
                .build();

        testWarehouse = Warehouse.builder()
                .id(2L)
                .name("Movement Warehouse")
                .location("Dock A")
                .build();

        testUser = User.builder()
                .id(3L)
                .username("operator")
                .email("op@test.com")
                .build();

        testMovement = StockMovement.builder()
                .id(10L)
                .product(testProduct)
                .warehouse(testWarehouse)
                .type(MovementType.INBOUND)
                .quantity(50)
                .referenceNumber("REF-SM-10")
                .performedBy(testUser)
                .notes("Inbound note")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createMovementDelegatesInboundToStockServiceReceive() {
        StockMovementRequest req = new StockMovementRequest(
                1L, 2L, MovementType.INBOUND, 50, "REF-SM-10", null, "Inbound note");

        when(stockService.receiveStock(1L, 2L, 50, "REF-SM-10", 3L, "Inbound note"))
                .thenReturn(testMovement);

        ApiResponse<?> response = stockMovementService.createMovement(req, 3L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isInstanceOf(StockMovementResponse.class);
        StockMovementResponse resData = (StockMovementResponse) response.getData();
        assertThat(resData.id()).isEqualTo(10L);
        assertThat(resData.quantity()).isEqualTo(50);
        assertThat(resData.type()).isEqualTo(MovementType.INBOUND);

        verify(stockService).receiveStock(1L, 2L, 50, "REF-SM-10", 3L, "Inbound note");
        verifyNoInteractions(movementRepository);
    }

    @Test
    void createMovementDelegatesOutboundToStockServiceDeduct() {
        StockMovementRequest req = new StockMovementRequest(
                1L, 2L, MovementType.OUTBOUND, 20, "REF-OUT", null, "Outbound note");

        StockMovement outMovement = StockMovement.builder()
                .id(11L)
                .product(testProduct)
                .warehouse(testWarehouse)
                .type(MovementType.OUTBOUND)
                .quantity(20)
                .referenceNumber("REF-OUT")
                .performedBy(testUser)
                .notes("Outbound note")
                .build();

        when(stockService.deductStock(1L, 2L, 20, "REF-OUT", 3L, "Outbound note"))
                .thenReturn(outMovement);

        ApiResponse<?> response = stockMovementService.createMovement(req, 3L);

        assertThat(response.isSuccess()).isTrue();
        StockMovementResponse resData = (StockMovementResponse) response.getData();
        assertThat(resData.type()).isEqualTo(MovementType.OUTBOUND);
        assertThat(resData.quantity()).isEqualTo(20);

        verify(stockService).deductStock(1L, 2L, 20, "REF-OUT", 3L, "Outbound note");
    }

    @Test
    void createMovementDelegatesAdjustmentToStockServiceAdjust() {
        StockMovementRequest req = new StockMovementRequest(
                1L, 2L, MovementType.ADJUSTMENT, 80, "REF-ADJ", null, "Adjust note");

        StockMovement adjMovement = StockMovement.builder()
                .id(12L)
                .product(testProduct)
                .warehouse(testWarehouse)
                .type(MovementType.ADJUSTMENT)
                .quantity(80)
                .referenceNumber("REF-ADJ")
                .performedBy(testUser)
                .notes("Adjust note")
                .build();

        when(stockService.adjustStock(1L, 2L, 80, "REF-ADJ", 3L, "Adjust note"))
                .thenReturn(adjMovement);

        ApiResponse<?> response = stockMovementService.createMovement(req, 3L);

        assertThat(response.isSuccess()).isTrue();
        StockMovementResponse resData = (StockMovementResponse) response.getData();
        assertThat(resData.type()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(resData.quantity()).isEqualTo(80);

        verify(stockService).adjustStock(1L, 2L, 80, "REF-ADJ", 3L, "Adjust note");
    }

    @Test
    void createMovementRejectsNullOrMissingType() {
        assertThrows(IllegalArgumentException.class, () -> stockMovementService.createMovement(null, 3L));
        StockMovementRequest emptyType = new StockMovementRequest(1L, 2L, null, 10, "REF", null, null);
        assertThrows(IllegalArgumentException.class, () -> stockMovementService.createMovement(emptyType, 3L));
    }

    @Test
    void getAllMovementsQueriesMovementRepositoryWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMovement> page = new PageImpl<>(List.of(testMovement), pageable, 1);

        when(movementRepository.findAll(pageable)).thenReturn(page);

        ApiResponse<?> response = stockMovementService.getAllMovements(null, null, null, pageable);

        assertThat(response.isSuccess()).isTrue();
        verify(movementRepository).findAll(pageable);
    }

    @Test
    void getAllMovementsFiltersByType() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMovement> page = new PageImpl<>(List.of(testMovement), pageable, 1);

        when(movementRepository.findByType(MovementType.INBOUND, pageable)).thenReturn(page);

        ApiResponse<?> response = stockMovementService.getAllMovements(MovementType.INBOUND, null, null, pageable);

        assertThat(response.isSuccess()).isTrue();
        verify(movementRepository).findByType(MovementType.INBOUND, pageable);
    }

    @Test
    void getMovementByIdReturnsMovementOrThrows() {
        when(movementRepository.findById(10L)).thenReturn(Optional.of(testMovement));

        ApiResponse<?> response = stockMovementService.getMovementById(10L);
        assertThat(response.isSuccess()).isTrue();
        StockMovementResponse data = (StockMovementResponse) response.getData();
        assertThat(data.id()).isEqualTo(10L);

        when(movementRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> stockMovementService.getMovementById(999L));
    }
}

