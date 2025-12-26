package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.dto.sale.SaleItemStatDto;

import java.math.BigDecimal;
import java.util.List;

public record ProductStatsDto(
        Long id,
        String name,
        String reference,
        BigDecimal price,
        Integer quantity,
        Integer seuilStock,
        Long categoryId,
        String categoryName,
        Boolean active,
        List<SaleItemStatDto> lastSale
) {}
