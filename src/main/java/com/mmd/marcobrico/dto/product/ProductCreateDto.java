package com.mmd.marcobrico.dto.product;

import java.math.BigDecimal;

public record ProductCreateDto(String name,
                               String reference,
                               BigDecimal price,
                               Integer quantity,
                               Integer seuilStock,
                               Long categoryId) {
}
