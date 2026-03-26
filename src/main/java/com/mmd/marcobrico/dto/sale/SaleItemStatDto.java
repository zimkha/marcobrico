package com.mmd.marcobrico.dto.sale;

import java.time.LocalDateTime;

public record SaleItemStatDto(
        Long saleItemId,
        Long saleId,
        Integer quantity,
        LocalDateTime saleDate
) {
}
