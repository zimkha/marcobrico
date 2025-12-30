package com.mmd.marcobrico.dto.sale;

import java.math.BigDecimal;

public record SaleItemDto(Long productId, int quantity, BigDecimal price) {}
