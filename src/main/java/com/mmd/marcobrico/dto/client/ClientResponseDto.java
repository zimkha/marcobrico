package com.mmd.marcobrico.dto.client;

import java.time.LocalDateTime;

public record ClientResponseDto(
        Long id,
        String name,
        String phone,
        String email,
        String address,
        LocalDateTime createdAt
) {}
