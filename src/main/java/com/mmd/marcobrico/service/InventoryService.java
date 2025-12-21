package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.inventory.InventoryCreateDto;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;

import java.util.List;

public interface InventoryService {

    InventoryResponseDto addEntry(InventoryCreateDto dto);

    List<InventoryResponseDto> getHistory(Long productId);
    InventoryResponseDto reverseMovement(Long inventoryId, Long userId, String comment);

}
