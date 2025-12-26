package com.mmd.marcobrico.mapper;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.dto.product.ProductCreateDto;
import com.mmd.marcobrico.dto.product.ProductResponseDto;
import com.mmd.marcobrico.dto.product.ProductUpdateDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {


    @ObjectFactory
    default Product toEntity(ProductCreateDto dto, @Context Category category) {
        Integer seuilStock = dto.seuilStock() != null ? dto.seuilStock() : 10;
        Integer quantity = dto.quantity() != null ? dto.quantity() : 0; // sécurité si quantity est null

        return Product.create(
                dto.name(),
                dto.reference(),
                dto.price(),
                quantity,
                seuilStock,
                category
        );
    }


    @ObjectFactory
    default Product updateEntity(ProductUpdateDto dto, @Context Product existing, @Context Category category) {
        Integer seuilStock = dto.seuilStock() != null ? dto.seuilStock() : (existing.getSeuilStock() != null ? existing.getSeuilStock() : 10);

        return existing.update(
                dto.name(),
                dto.price(),
                seuilStock,
                category
        );
    }

    default ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getReference(),
                product.getPrice(),
                product.getQuantity() != null ? product.getQuantity() : 0,
                product.getSeuilStock() != null ? product.getSeuilStock() : 10,
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}
