package com.mmd.marcobrico.mapper;


import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.dto.sale.SaleCreateDto;
import com.mmd.marcobrico.dto.sale.SaleItemResponseDto;
import com.mmd.marcobrico.dto.sale.SaleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    default Sale toEntity(SaleCreateDto dto) {
        return null;
    }

    default SaleResponseDto toDto(Sale sale) {
        List<SaleItemResponseDto> itemDtos = sale.getItems().stream()
                .map(i -> new SaleItemResponseDto(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice()
                ))
                .toList();

        return new SaleResponseDto(
                sale.getId(),
                sale.getUser().getId(),
                sale.getUser().getUsername(),
                itemDtos,
                sale.getTotal(),
                sale.isCanceled(),
                sale.getCreatedAt()
        );
    }
}
