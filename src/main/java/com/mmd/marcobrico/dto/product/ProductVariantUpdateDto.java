package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record ProductVariantUpdateDto(
        UnitType saleUnit,
        @DecimalMin(value = "0.0001", inclusive = true)
        @Digits(integer = 10, fraction = 4)
        BigDecimal conversionFactor,

        @DecimalMin(value = "0.00", inclusive = true)
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        Boolean wholesale
) {
}
