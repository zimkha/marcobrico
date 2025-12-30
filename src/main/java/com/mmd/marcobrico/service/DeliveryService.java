package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.DeliveryStatus;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateDto;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import org.springframework.data.domain.Page;

public interface DeliveryService {

    DeliveryResponseDto create(DeliveryCreateDto dto);

    DeliveryResponseDto dispatch(Long deliveryId, String trackingNumber);

    DeliveryResponseDto deliver(Long deliveryId);

    DeliveryResponseDto cancel(Long deliveryId);

    DeliveryResponseDto createDeliveryFromReceivedSupply(Long supplyId, Long clientId, String address);

    Page<DeliveryResponseDto> search(
            Long clientId,
            Long carrierId,
            DeliveryStatus status,
            int page,
            int size,
            String sortBy,

            String sortDirection
    );
}

