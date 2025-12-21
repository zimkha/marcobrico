package com.mmd.marcobrico.dto.category;

import java.time.LocalDateTime;

public record CategoryResponseDto(Long id,
                                  String name,
                                  String description,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
}
