package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.BaseUnit;

import java.math.BigDecimal;

public record ProductUpdateDto(String name,
                                       BaseUnit baseUnit,
                                       Long categoryId,
                                       Boolean active) {
}
