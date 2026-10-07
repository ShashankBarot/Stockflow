package com.inventory.management.service;

import com.inventory.management.dto.request.DispatchOrderRequest;
import com.inventory.management.dto.request.SaleOrderItemRequest;
import com.inventory.management.dto.request.SaleOrderRequest;
import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.SaleOrderResponse;
import com.inventory.management.entity.Customer;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.SaleOrder;
import com.inventory.management.entity.SaleOrderItem;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.CustomerRepository;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.SaleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(InventoryService.class)
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService {

    private final SaleOrderRepository saleOrders;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final InventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllSaleOrders(Pageable pageable) {
        return ApiResponse.ok(saleOrders.findAll(pageable).map(SaleOrderResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getSaleOrderById(Long id) {
        SaleOrder order = saleOrders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with id: " + id));
        return ApiResponse.ok(SaleOrderResponse.from(order));
    }

    @Override
    @Transactional
    public ApiResponse<?> createSaleOrder(SaleOrderRequest request) {
        Customer customer = customers.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        SaleOrder order = SaleOrder.builder()
                .customer(customer)
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build();

        for (SaleOrderItemRequest itemRequest : request.getItems()) {
            Product product = products.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            SaleOrderItem item = SaleOrderItem.builder()
                    .saleOrder(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .build();
            order.getItems().add(item);
        }

        SaleOrder savedOrder = saleOrders.save(order);
        return ApiResponse.ok("Sale Order created", SaleOrderResponse.from(savedOrder));
    }

    @Override
    @Transactional
    public ApiResponse<?> dispatchSaleOrder(Long id, DispatchOrderRequest request, Long userId) {
        SaleOrder order = saleOrders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with id: " + id));

        if (order.getStatus() != SaleOrder.OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING state");
        }

        // Trigger outbound movements for each item
        for (SaleOrderItem item : order.getItems()) {
            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(item.getProduct().getId());
            movementRequest.setWarehouseId(request.getWarehouseId());
            movementRequest.setType(MovementType.OUTBOUND);
            movementRequest.setQuantity(item.getQuantity());
            movementRequest.setReferenceNumber(order.getReferenceNumber() != null ? order.getReferenceNumber() : "SO-" + order.getId());
            movementRequest.setNotes("Dispatched for Sale Order " + order.getId());

            inventoryService.createMovement(movementRequest, userId);
        }

        order.setStatus(SaleOrder.OrderStatus.DISPATCHED);
        saleOrders.save(order);

        return ApiResponse.ok("Sale Order dispatched successfully", SaleOrderResponse.from(order));
    }
}
