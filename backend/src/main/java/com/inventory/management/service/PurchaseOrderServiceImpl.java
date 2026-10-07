package com.inventory.management.service;

import com.inventory.management.dto.request.PurchaseOrderItemRequest;
import com.inventory.management.dto.request.PurchaseOrderRequest;
import com.inventory.management.dto.request.ReceiveOrderRequest;
import com.inventory.management.dto.request.StockMovementRequest;
import com.inventory.management.dto.response.ApiResponse;
import com.inventory.management.dto.response.PurchaseOrderResponse;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.PurchaseOrder;
import com.inventory.management.entity.PurchaseOrderItem;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.entity.Supplier;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.PurchaseOrderRepository;
import com.inventory.management.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(InventoryService.class)
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrders;
    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final InventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getAllPurchaseOrders(Pageable pageable) {
        return ApiResponse.ok(purchaseOrders.findAll(pageable).map(PurchaseOrderResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getPurchaseOrderById(Long id) {
        PurchaseOrder order = purchaseOrders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));
        return ApiResponse.ok(PurchaseOrderResponse.from(order));
    }

    @Override
    @Transactional
    public ApiResponse<?> createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = suppliers.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build();

        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Product product = products.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .build();
            order.getItems().add(item);
        }

        PurchaseOrder savedOrder = purchaseOrders.save(order);
        return ApiResponse.ok("Purchase Order created", PurchaseOrderResponse.from(savedOrder));
    }

    @Override
    @Transactional
    public ApiResponse<?> receivePurchaseOrder(Long id, ReceiveOrderRequest request, Long userId) {
        PurchaseOrder order = purchaseOrders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));

        if (order.getStatus() != PurchaseOrder.OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING state");
        }

        // Trigger inbound movements for each item
        for (PurchaseOrderItem item : order.getItems()) {
            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(item.getProduct().getId());
            movementRequest.setWarehouseId(request.getWarehouseId());
            movementRequest.setType(MovementType.INBOUND);
            movementRequest.setQuantity(item.getQuantity());
            movementRequest.setReferenceNumber(order.getReferenceNumber() != null ? order.getReferenceNumber() : "PO-" + order.getId());
            movementRequest.setNotes("Received from Purchase Order " + order.getId());

            inventoryService.createMovement(movementRequest, userId);
        }

        order.setStatus(PurchaseOrder.OrderStatus.RECEIVED);
        purchaseOrders.save(order);

        return ApiResponse.ok("Purchase Order received successfully", PurchaseOrderResponse.from(order));
    }
}
