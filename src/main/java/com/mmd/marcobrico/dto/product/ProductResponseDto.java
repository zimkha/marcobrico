package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.BaseUnit;

public record ProductResponseDto(Long id,
                                 String name,
                                 String reference,
                                 BaseUnit baseUnit,
                                 Long categoryId,
                                 String categoryName,
                                 Boolean active) {
}
