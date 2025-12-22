package com.mmd.marcobrico.dto.partner;

import com.mmd.marcobrico.domain.PartnerType;

public record PartnerCreateDto(
        String name,
        PartnerType type,
        String phone,
        String email,
        String address
) {}
