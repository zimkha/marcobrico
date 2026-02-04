package com.mmd.marcobrico.mapper;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.dto.product.ProductCreateDto;
import com.mmd.marcobrico.dto.product.ProductResponseDto;
import com.mmd.marcobrico.dto.product.ProductUpdateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    default Product toEntity(ProductCreateDto dto, Category category) {
        return Product.builder()
                .name(dto.name())
                .reference(dto.reference())
                .category(category)
                .active(true)
                .baseUnit(dto.baseUnit())
                .build();
    }
    default void updateEntity(ProductUpdateDto dto, Product product, Category category) {

        if (dto.name() != null) {
            product.setName(dto.name());
        }
        if (dto.baseUnit() != null) {
            product.setBaseUnit(dto.baseUnit());
        }
        if (category != null) {
            product.setCategory(category);
        }
        if (dto.active() != null) {
            product.setActive(dto.active());
        }
    }

    default ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getReference(),
                product.getBaseUnit(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.isActive()
        );
    }
}
