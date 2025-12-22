package com.mmd.marcobrico.mapper;


import com.mmd.marcobrico.domain.Delivery;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    default DeliveryResponseDto toDto(Delivery delivery) {
        return new DeliveryResponseDto(
                delivery.getId(),
                delivery.getSale().getId(),
                delivery.getCarrier().getId(),
                delivery.getCarrier().getName(),
                delivery.getStatus(),
                delivery.getTrackingNumber(),
                delivery.getCreatedAt()
        );
    }
}
