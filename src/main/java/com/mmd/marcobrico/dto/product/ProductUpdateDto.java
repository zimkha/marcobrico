package com.mmd.marcobrico.dto.product;

import java.math.BigDecimal;

public record ProductUpdateDto(String name,
                               BigDecimal price,
                               Integer seuilStock,
                               Long categoryId) {
}
