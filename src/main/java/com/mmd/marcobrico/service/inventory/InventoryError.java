package com.mmd.marcobrico.service.inventory;

public sealed interface InventoryError {
    record ProductNotFound(Long productId) implements InventoryError {}
    record InsufficientStock(int required, int available) implements InventoryError {}
    record UserNotAuthenticated() implements InventoryError {}
    record DatabaseError(String message) implements InventoryError{}
}
