package com.inventory.management.service;

import com.inventory.management.entity.Inventory;
import com.inventory.management.entity.Notification.NotificationType;
import com.inventory.management.entity.Product;
import com.inventory.management.entity.StockMovement;
import com.inventory.management.entity.StockMovement.MovementType;
import com.inventory.management.entity.User;
import com.inventory.management.entity.Warehouse;
import com.inventory.management.exception.InsufficientStockException;
import com.inventory.management.exception.ResourceNotFoundException;
import com.inventory.management.repository.InventoryRepository;
import com.inventory.management.repository.MovementRepository;
import com.inventory.management.repository.ProductRepository;
import com.inventory.management.repository.UserRepository;
import com.inventory.management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final InventoryRepository inventoryRepository;
    private final MovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public StockMovement receiveStock(Long productId, Long warehouseId, Integer quantity, String referenceNumber, Long performedByUserId, String notes) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Receive quantity must be positive");
        }

        Product product = findProductOrThrow(productId);
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);
        User user = findUserOrThrow(performedByUserId);

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .minThreshold(0)
                        .build());

        int oldQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        int newQuantity = oldQuantity + quantity;
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        notifyIfLowStock(product, warehouse, inventory, oldQuantity, newQuantity);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .type(MovementType.INBOUND)
                .quantity(quantity)
                .referenceNumber(referenceNumber)
                .performedBy(user)
                .notes(notes)
                .build();

        return movementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement deductStock(Long productId, Long warehouseId, Integer quantity, String referenceNumber, Long performedByUserId, String notes) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Deduct quantity must be positive");
        }

        Product product = findProductOrThrow(productId);
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);
        User user = findUserOrThrow(performedByUserId);

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseThrow(() -> new InsufficientStockException(productId, warehouseId, quantity, 0));

        int currentQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        if (quantity > currentQuantity) {
            throw new InsufficientStockException(productId, warehouseId, quantity, currentQuantity);
        }

        int newQuantity = currentQuantity - quantity;
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        notifyIfLowStock(product, warehouse, inventory, currentQuantity, newQuantity);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .type(MovementType.OUTBOUND)
                .quantity(quantity)
                .referenceNumber(referenceNumber)
                .performedBy(user)
                .notes(notes)
                .build();

        return movementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement adjustStock(Long productId, Long warehouseId, Integer newQuantity, String referenceNumber, Long performedByUserId, String notes) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Adjust quantity cannot be negative or null");
        }

        Product product = findProductOrThrow(productId);
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);
        User user = findUserOrThrow(performedByUserId);

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .minThreshold(0)
                        .build());

        int oldQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        notifyIfLowStock(product, warehouse, inventory, oldQuantity, newQuantity);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .type(MovementType.ADJUSTMENT)
                .quantity(newQuantity)
                .referenceNumber(referenceNumber)
                .performedBy(user)
                .notes(notes)
                .build();

        return movementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement transferStock(Long productId, Long sourceWarehouseId, Long destinationWarehouseId, Integer quantity, String referenceNumber, Long performedByUserId, String notes) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be positive");
        }
        if (sourceWarehouseId == null || destinationWarehouseId == null) {
            throw new IllegalArgumentException("Source and destination warehouse IDs are required");
        }
        if (sourceWarehouseId.equals(destinationWarehouseId)) {
            throw new IllegalArgumentException("Source and destination warehouses must be different");
        }

        Product product = findProductOrThrow(productId);
        Warehouse source = findWarehouseOrThrow(sourceWarehouseId);
        Warehouse destination = findWarehouseOrThrow(destinationWarehouseId);
        User user = findUserOrThrow(performedByUserId);

        Inventory sourceInventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(productId, sourceWarehouseId)
                .orElseThrow(() -> new InsufficientStockException(productId, sourceWarehouseId, quantity, 0));
        int sourceQuantity = sourceInventory.getQuantity() != null ? sourceInventory.getQuantity() : 0;
        if (quantity > sourceQuantity) {
            throw new InsufficientStockException(productId, sourceWarehouseId, quantity, sourceQuantity);
        }

        Inventory destinationInventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(productId, destinationWarehouseId)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .warehouse(destination)
                        .quantity(0)
                        .minThreshold(0)
                        .build());

        int destinationQuantity = destinationInventory.getQuantity() != null ? destinationInventory.getQuantity() : 0;
        int newSourceQuantity = sourceQuantity - quantity;
        int newDestinationQuantity = destinationQuantity + quantity;
        sourceInventory.setQuantity(newSourceQuantity);
        destinationInventory.setQuantity(newDestinationQuantity);
        inventoryRepository.save(sourceInventory);
        inventoryRepository.save(destinationInventory);
        notifyIfLowStock(product, source, sourceInventory, sourceQuantity, newSourceQuantity);
        notifyIfLowStock(product, destination, destinationInventory, destinationQuantity, newDestinationQuantity);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .warehouse(source)
                .destinationWarehouse(destination)
                .type(MovementType.TRANSFER)
                .quantity(quantity)
                .referenceNumber(referenceNumber)
                .performedBy(user)
                .notes((notes == null ? "" : notes + " — ") + "Transferred to warehouse " + destination.getId())
                .build();
        return movementRepository.save(movement);
    }

    private Product findProductOrThrow(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Warehouse findWarehouseOrThrow(Long warehouseId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException("Warehouse ID is required");
        }
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
    }

    private User findUserOrThrow(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void notifyIfLowStock(Product product, Warehouse warehouse, Inventory inventory,
                                  int oldQuantity, int newQuantity) {
        int threshold = inventory.getMinThreshold() != null ? inventory.getMinThreshold() : 0;
        if (oldQuantity > threshold && newQuantity <= threshold) {
            String title = "Low stock: " + product.getName();
            String message = product.getName() + " (" + product.getSku() + ") at " + warehouse.getName()
                    + " has " + newQuantity + " units remaining. Minimum threshold: " + threshold + ".";
            userRepository.findAll().forEach(user -> notificationService.createNotification(
                    title, message, NotificationType.LOW_STOCK, user.getId()));
        }
    }
}
