package com.mmd.marcobrico.dto.product;

import com.mmd.marcobrico.domain.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductVariantCreateDto(
        @NotNull(message = "L'unité de vente est obligatoire")
        UnitType saleUnit,

        @NotNull(message = "Le facteur de conversion est obligatoire")
        @DecimalMin(value = "0.0001", inclusive = true,
                message = "Le facteur de conversion doit être > 0")
        @Digits(integer = 10, fraction = 4,
                message = "Format invalide (max 10 chiffres entiers, 4 décimales)")
        BigDecimal conversionFactor,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.00", inclusive = true,
                message = "Le prix ne peut pas être négatif")
        @Digits(integer = 10, fraction = 2,
                message = "Format invalide (max 10 chiffres entiers, 2 décimales)")
        BigDecimal price,

        @NotNull(message = "Le stock initial est obligatoire")
        @DecimalMin(value = "0.00", inclusive = true,
                message = "Le stock ne peut pas être négatif")
        @Digits(integer = 10, fraction = 4)
        BigDecimal initialStock,

        Boolean wholesale
) {
}
