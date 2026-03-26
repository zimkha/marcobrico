package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.UnitType;

import java.math.BigDecimal;

public record ProductVariantResponseDto(
        Long id,
        UnitType saleUnit,
        BigDecimal conversionFactor,
        BigDecimal price,
        BigDecimal stock,
        Boolean wholesale
) {
}
