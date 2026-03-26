package com.mmd.marcobrico.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateDto(@NotBlank String name,
                                String description) {
}
