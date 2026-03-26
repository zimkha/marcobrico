package com.mmd.marcobrico.dto.payment;

import com.mmd.marcobrico.domain.PaymentMethod;

import java.math.BigDecimal;

public record PaymentCreateDto(
        Long invoiceId,
        PaymentMethod method,
        BigDecimal amount,
        String reference
) {}
