package com.mmd.marcobrico.dto.user;

import com.mmd.marcobrico.domain.Role;

public record RegisterRequest(
        String username,
        String email,
        String password,
        Role role
) {}
