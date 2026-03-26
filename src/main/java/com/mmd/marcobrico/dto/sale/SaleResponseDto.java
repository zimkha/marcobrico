package com.mmd.marcobrico.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record SaleResponseDto(
        Long id,
        Long userId,
        String username,
        Long clientId,
        String clientName,
        List<SaleItemResponseDto> items,
        BigDecimal total,
        boolean canceled,
        LocalDateTime createdAt
) {}
