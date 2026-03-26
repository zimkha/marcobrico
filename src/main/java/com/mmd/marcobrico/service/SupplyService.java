package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.SupplyStatus;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.dto.supply.SupplyCreateDto;
import com.mmd.marcobrico.dto.supply.SupplyResponseDto;
import org.springframework.data.domain.Page;

public interface SupplyService {

    SupplyResponseDto create(SupplyCreateDto dto);

    SupplyResponseDto receive(Long supplyId);

    SupplyResponseDto cancel(Long supplyId);

    Page<SupplyResponseDto> search(
            Long supplierId,
            SupplyStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
    DeliveryResponseDto createDeliveryFromSupply(Long supplyId, Long clientId, String address);
}
