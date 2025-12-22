package com.mmd.marcobrico.dto.supply;

public record SupplyItemResponseDto(
        Long productId,
        String productName,
        int quantity
) {}