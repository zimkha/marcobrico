package com.mmd.marcobrico.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailDto(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        long numberOfSales,
        LocalDateTime lastSaleDate,
        BigDecimal revenue
) {}
