package com.mmd.marcobrico.mapper;



import com.mmd.marcobrico.domain.Partner;
import com.mmd.marcobrico.dto.partner.PartnerResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartnerMapper {
    default PartnerResponseDto toDto(Partner partner) {
        return new PartnerResponseDto(
                partner.getId(),
                partner.getName(),
                partner.getType(),
                partner.getPhone(),
                partner.getEmail(),
                partner.getAddress(),
                partner.getCreatedAt()
        );
    }
}
