package com.mmd.marcobrico.dto.inventory;

public record InventoryCreateDto(  Long productId,
                                   int quantityChange,
                                   String comment) {
}
