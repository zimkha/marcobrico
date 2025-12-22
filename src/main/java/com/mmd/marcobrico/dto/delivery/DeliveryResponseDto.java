package com.mmd.marcobrico.dto.delivery;

import com.mmd.marcobrico.domain.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponseDto(
        Long id,
        Long saleId,
        Long carrierId,
        String carrierName,
        DeliveryStatus status,
        String trackingNumber,
        LocalDateTime createdAt
) {}
