package com.mmd.marcobrico.dto.inventory;

import java.time.LocalDateTime;

public record InventoryFilterDto(
        String productName,
        String type,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long userId,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
