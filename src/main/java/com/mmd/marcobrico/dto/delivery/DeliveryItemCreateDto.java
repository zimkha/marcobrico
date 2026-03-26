package com.mmd.marcobrico.dto.delivery;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DeliveryItemCreateDto(

        @NotNull
        Long productId,

        @Min(1)
        int quantity
) {
}
