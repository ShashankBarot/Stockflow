package com.inventory.management.exception;

/**
 * Thrown when a stock deduction request exceeds the available inventory quantity
 * for a given product + warehouse combination.
 *
 * <p>Maps to HTTP 409 CONFLICT: the request is valid but conflicts with the
 * current inventory state. The quantity has NOT been modified.
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Long warehouseId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long productId, Long warehouseId, int requested, int available) {
        super(String.format(
                "Insufficient stock for product %d in warehouse %d: requested %d, available %d",
                productId, warehouseId, requested, available));
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.requested = requested;
        this.available = available;
    }

    public Long getProductId()  { return productId; }
    public Long getWarehouseId() { return warehouseId; }
    public int getRequested()   { return requested; }
    public int getAvailable()   { return available; }
}

