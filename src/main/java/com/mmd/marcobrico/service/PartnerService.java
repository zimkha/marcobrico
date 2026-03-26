package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.PartnerType;
import com.mmd.marcobrico.dto.partner.PartnerCreateDto;
import com.mmd.marcobrico.dto.partner.PartnerResponseDto;
import org.springframework.data.domain.Page;
public interface PartnerService {
    PartnerResponseDto create(PartnerCreateDto dto);

    Page<PartnerResponseDto> search(
            String name,
            PartnerType type,
            String phone,
            String email,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
}
