package com.mmd.marcobrico.dto.sale;

import java.util.List;
public record SaleCreateDto(Long userId, List<SaleItemDto> items) {}