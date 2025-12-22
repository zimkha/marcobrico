package com.mmd.marcobrico.dto.supply;

import com.mmd.marcobrico.domain.SupplyStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SupplyResponseDto(
        Long id,
        Long supplierId,
        String supplierName,
        SupplyStatus status,
        List<SupplyItemResponseDto> items,
        LocalDateTime createdAt
) {}
