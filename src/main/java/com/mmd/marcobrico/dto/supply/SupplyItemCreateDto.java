package com.mmd.marcobrico.dto.supply;

import java.math.BigDecimal;

public record SupplyItemCreateDto(
        Long productId,
        int quantity,
        BigDecimal price
) {}
