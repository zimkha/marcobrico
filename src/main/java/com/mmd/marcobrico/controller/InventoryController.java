package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.inventory.InventoryCreateDto;
import com.mmd.marcobrico.dto.inventory.InventoryFilterDto;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;
import com.mmd.marcobrico.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;


    @GetMapping("/{productId}/history")
    public List<InventoryResponseDto> getHistory(@PathVariable Long productId) {
        return service.getHistory(productId);
    }

    @GetMapping("/product/{productId}")
    public List<InventoryResponseDto> productHistory(@PathVariable Long productId) {
        return service.getHistory(productId);
    }

    @PostMapping
    public InventoryResponseDto create(@RequestBody @Valid InventoryCreateDto dto) {
        return service.addEntry(dto);
    }

    @PostMapping("/{id}/reverse")
    public InventoryResponseDto reverse(
            @PathVariable Long id,
            @RequestParam String comment
    ) {
        return service.reverseMovement(id, null, comment);
    }

    @GetMapping("/search")
    public Page<InventoryResponseDto> search(InventoryFilterDto dto) {
        return service.search(dto);
    }
}
