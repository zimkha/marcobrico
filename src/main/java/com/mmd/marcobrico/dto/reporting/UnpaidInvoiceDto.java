package com.mmd.marcobrico.dto.reporting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UnpaidInvoiceDto(
        Long invoiceId,
        String invoiceNumber,
        String clientName,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal unpaidAmount,
        LocalDateTime issuedAt
) {}
