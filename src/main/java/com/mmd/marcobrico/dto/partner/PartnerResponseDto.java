package com.mmd.marcobrico.dto.partner;

import com.mmd.marcobrico.domain.PartnerType;
import java.time.LocalDateTime;

public record PartnerResponseDto(
        Long id,
        String name,
        PartnerType type,
        String phone,
        String email,
        String address,
        LocalDateTime createdAt
) {}
