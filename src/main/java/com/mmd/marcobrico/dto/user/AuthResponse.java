package com.mmd.marcobrico.dto.user;


public record AuthResponse(
        String token,

        String username,
        String role
) {}
