package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.BaseUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailDto(
        Long id,
        String name,
        String reference,
        BaseUnit baseUnit,
        BigDecimal stock, // unité de base
        List<ProductVariantResponseDto> variants
) {}
