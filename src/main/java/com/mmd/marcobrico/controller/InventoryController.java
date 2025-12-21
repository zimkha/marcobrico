package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.inventory.InventoryCreateDto;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;
import com.mmd.marcobrico.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping
    public InventoryResponseDto addEntry(@RequestBody @Valid InventoryCreateDto dto) {
        return service.addEntry(dto);
    }

    @GetMapping("/{productId}/history")
    public List<InventoryResponseDto> getHistory(@PathVariable Long productId) {
        return service.getHistory(productId);
    }
}
