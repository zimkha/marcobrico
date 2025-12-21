package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.sale.SaleCreateDto;
import com.mmd.marcobrico.dto.sale.SaleResponseDto;
import com.mmd.marcobrico.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @PostMapping
    public SaleResponseDto create(@RequestBody @Valid SaleCreateDto dto) {
        return saleService.createSale(dto);
    }

    @PostMapping("/{id}/cancel")
    public SaleResponseDto cancel(@PathVariable Long id,
                                  @RequestParam Long userId,
                                  @RequestParam String comment) {
        return saleService.cancelSale(id, comment);
    }

    @GetMapping("/search")
    public Page<SaleResponseDto> search(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return saleService.searchSales(productId, userId, startDate, endDate, page, size, sortBy, sortDirection);
    }
}
