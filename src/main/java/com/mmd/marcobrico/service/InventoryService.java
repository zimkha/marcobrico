package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.dto.inventory.InventoryCreateDto;
import com.mmd.marcobrico.dto.inventory.InventoryFilterDto;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    InventoryResponseDto addEntry(InventoryCreateDto dto);

    List<InventoryResponseDto> getHistory(Long productId);
    InventoryResponseDto reverseMovement(Long inventoryId, Long userId, String comment);
    Page<InventoryResponseDto> search(InventoryFilterDto dto);
    void deductStock(Product product, int quantity, String reason);
    void addStock(Product product, int quantity, String reason);
    void applyInventoryAdjustment(Product product, int newQuantity, String reason);

}
