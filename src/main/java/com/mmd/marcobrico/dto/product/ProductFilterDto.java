package com.mmd.marcobrico.dto.product;

public record ProductFilterDto(
        String name,
        Long categoryId,
        Boolean belowThreshold,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
