package com.mmd.marcobrico.dto.inventory;

import com.mmd.marcobrico.domain.User;

import java.time.LocalDateTime;

public record InventoryResponseDto(Long id,
                                   Long productId,
                                   String productName,
                                   int quantityBefore,
                                   int quantityAfter,
                                   String type,
                                   String comment,
                                   LocalDateTime createdAt, String username) {

}
