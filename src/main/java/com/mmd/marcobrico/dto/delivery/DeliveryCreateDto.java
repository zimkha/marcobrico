package com.mmd.marcobrico.dto.delivery;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeliveryCreateDto(

        @NotNull
        Long clientId,

        @NotNull
        String address,

        @NotEmpty
        List<DeliveryItemCreateDto> items
) {
}
