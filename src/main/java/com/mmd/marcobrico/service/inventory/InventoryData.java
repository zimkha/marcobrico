package com.mmd.marcobrico.service.inventory;

import com.mmd.marcobrico.domain.InventoryType;
import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.User;

public record InventoryData(
        Product product,
        int quantityBefore,
        int quantityAfter,
        InventoryType type,
        User user
) {
}
