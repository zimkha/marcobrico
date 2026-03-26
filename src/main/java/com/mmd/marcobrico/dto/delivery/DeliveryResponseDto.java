package com.mmd.marcobrico.dto.delivery;

import com.mmd.marcobrico.domain.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponseDto(
        Long id,
        Long clientId,
        DeliveryStatus status,
        String address,
        String trackingNumber,
        LocalDateTime createdAt,
        Long saleId
) {}

