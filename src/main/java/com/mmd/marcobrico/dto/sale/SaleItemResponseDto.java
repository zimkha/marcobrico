package com.mmd.marcobrico.dto.sale;

import java.math.BigDecimal;

public record SaleItemResponseDto(
        Long productId,
        String productName,
        int quantity,
        BigDecimal price
) {}
