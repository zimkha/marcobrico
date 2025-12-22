package com.mmd.marcobrico.dto.supply;

import java.util.List;

public record SupplyCreateDto(
        Long supplierId,
        List<SupplyItemCreateDto> items
) {}
