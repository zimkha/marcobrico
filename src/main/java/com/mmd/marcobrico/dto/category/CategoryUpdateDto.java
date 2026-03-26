package com.mmd.marcobrico.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateDto(@NotBlank String name,
                                String description) {
}
