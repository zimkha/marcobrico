package com.mmd.marcobrico.mapper;


import com.mmd.marcobrico.domain.Supply;
import com.mmd.marcobrico.dto.supply.SupplyItemResponseDto;
import com.mmd.marcobrico.dto.supply.SupplyResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplyMapper {
    default SupplyResponseDto toDto(Supply supply) {
        return new SupplyResponseDto(
                supply.getId(),
                supply.getSupplier().getId(),
                supply.getSupplier().getName(),
                supply.getStatus(),
                supply.getItems().stream()
                        .map(i -> new SupplyItemResponseDto(
                                i.getProduct().getId(),
                                i.getProduct().getName(),
                                i.getQuantity()
                        ))
                        .toList(),
                supply.getCreatedAt()
        );
    }
}
