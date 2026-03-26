package com.mmd.marcobrico.mapper;


import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.ProductVariant;
import com.mmd.marcobrico.dto.product.ProductVariantCreateDto;
import com.mmd.marcobrico.dto.product.ProductVariantResponseDto;
import com.mmd.marcobrico.dto.product.ProductVariantUpdateDto;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

      /* =======================
       CREATE
     ======================= */

    default ProductVariant toEntity(ProductVariantCreateDto dto, Product product) {
        return ProductVariant.builder()
                .product(product)
                .saleUnit(dto.saleUnit())
                .conversionToBaseUnit(dto.conversionFactor())
                .price(dto.price())
                .stock(dto.initialStock())  // ✅ Stock initial du DTO
                .wholesale(dto.wholesale() != null ? dto.wholesale() : false)
                .build();
    }

    /* =======================
       UPDATE
     ======================= */

    default void updateEntity(ProductVariantUpdateDto dto, ProductVariant variant) {
        if (dto.saleUnit() != null) {
            variant.setSaleUnit(dto.saleUnit());
        }
        if (dto.conversionFactor() != null) {
            variant.setConversionToBaseUnit(dto.conversionFactor());
        }
        if (dto.price() != null) {
            variant.setPrice(dto.price());
        }
        if (dto.wholesale() != null) {
            variant.setWholesale(dto.wholesale());
        }
    }

    /* =======================
       RESPONSE
     ======================= */
    default ProductVariantResponseDto toDto(ProductVariant variant) {
        return new ProductVariantResponseDto(
                variant.getId(),
                variant.getSaleUnit(),
                variant.getConversionToBaseUnit(),
                variant.getPrice(),
                variant.getStock(),
                variant.isWholesale()
        );
    }

}
