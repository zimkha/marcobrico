package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.BaseUnit;

public record ProductCreateDto(  String name,
                                 String reference,
                                 BaseUnit baseUnit,
                                 Long categoryId) {
}
