package com.mmd.marcobrico.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StockAdjustmentDto(
        @NotNull(message = "La quantité est obligatoire")
        @DecimalMin(value = "0.01", inclusive = true,
                message = "La quantité doit être positive")
        @Digits(integer = 10, fraction = 4)
        BigDecimal quantity,

        @Size(max = 500, message = "Le motif ne peut dépasser 500 caractères")
        String reason
) {
}
