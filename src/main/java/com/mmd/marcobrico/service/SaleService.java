package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.Delivery;
import com.mmd.marcobrico.dto.sale.SaleCreateDto;
import com.mmd.marcobrico.dto.sale.SaleResponseDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SaleService {
    SaleResponseDto createSale(SaleCreateDto dto);

    SaleResponseDto cancelSale(Long saleId, String comment);

    Page<SaleResponseDto> searchSales(
            Long productId,
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    SaleResponseDto getSaleDetail(Long saleId);
    SaleResponseDto createFromDelivery(Delivery delivery);
    long getTotalSales();
}
