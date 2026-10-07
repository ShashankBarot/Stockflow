package com.inventory.management.service;

import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.request.TransferOrderItemRequest;
import com.inventory.management.dto.request.TransferOrderRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.TransferOrderResponse;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.entity.TransferOrder;
import com.inventory.management.entity.TransferOrderItem;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.TransferOrderRepository;
import com.inventory.management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(InventoryService.class)
public class TransferOrderServiceImpl implements TransferOrderService {

    private final TransferOrderRepository transferOrders;
    private final WarehouseRepository warehouses;
    private final ProductRepository products;
    private final InventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllTransferOrders(Pageable pageable) {
        return ApiResponse.ok(transferOrders.findAll(pageable).map(TransferOrderResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getTransferOrderById(Long id) {
        TransferOrder order = transferOrders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer Order not found with id: " + id));
        return ApiResponse.ok(TransferOrderResponse.from(order));
    }

    @Override
    @Transactional
    public ApiResponse<?> createTransferOrder(TransferOrderRequest request, Long userId) {
        Warehouse source = warehouses.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source warehouse not found"));

        Warehouse destination = warehouses.findById(request.getDestinationWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination warehouse not found"));
                
        if (source.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("Source and destination warehouses must be different");
        }

        TransferOrder order = TransferOrder.builder()
                .sourceWarehouse(source)
                .destinationWarehouse(destination)
                .referenceNumber(request.getReferenceNumber())
                .build();

        for (TransferOrderItemRequest itemRequest : request.getItems()) {
            Product product = products.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            TransferOrderItem item = TransferOrderItem.builder()
                    .transferOrder(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .build();
            order.getItems().add(item);
        }

        TransferOrder savedOrder = transferOrders.save(order);

        // Process transfers
        for (TransferOrderItem item : savedOrder.getItems()) {
            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(item.getProduct().getId());
            movementRequest.setWarehouseId(source.getId());
            movementRequest.setDestinationWarehouseId(destination.getId());
            movementRequest.setType(MovementType.TRANSFER);
            movementRequest.setQuantity(item.getQuantity());
            movementRequest.setReferenceNumber(savedOrder.getReferenceNumber() != null ? savedOrder.getReferenceNumber() : "TR-" + savedOrder.getId());
            movementRequest.setNotes("Transfer Order " + savedOrder.getId());

            inventoryService.createMovement(movementRequest, userId);
        }

        savedOrder.setStatus(TransferOrder.TransferStatus.COMPLETED);
        
        return ApiResponse.ok("Transfer Order executed successfully", TransferOrderResponse.from(savedOrder));
    }
}
