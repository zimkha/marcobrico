package com.mmd.marcobrico.dto.client;

public record ClientCreateDto(
        String name,
        String phone,
        String email,
        String address
) {}
