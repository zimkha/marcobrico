package com.mmd.marcobrico.mapper;

import com.mmd.marcobrico.domain.Client;
import com.mmd.marcobrico.dto.client.ClientResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    default ClientResponseDto toDto(Client client) {
        return new ClientResponseDto(
                client.getId(),
                client.getName(),
                client.getPhone(),
                client.getEmail(),
                client.getAddress(),
                client.getCreatedAt()
        );
    }
}
