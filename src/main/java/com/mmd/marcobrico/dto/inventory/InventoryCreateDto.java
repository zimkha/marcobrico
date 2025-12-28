package com.mmd.marcobrico.dto.inventory;

import com.mmd.marcobrico.domain.InventoryType;

public record InventoryCreateDto(Long productId,
                                 int quantityChange,
                                 InventoryType type,
                                 String comment) {
}
