package com.mmd.marcobrico.dto.invoice;

import com.mmd.marcobrico.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponseDto(
        Long id,
        String invoiceNumber,
        Long saleId,
        InvoiceStatus status,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        LocalDateTime issuedAt
) {}
