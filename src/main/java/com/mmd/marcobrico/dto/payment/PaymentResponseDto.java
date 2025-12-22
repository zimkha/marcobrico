package com.mmd.marcobrico.dto.payment;

import com.mmd.marcobrico.domain.PaymentMethod;
import com.mmd.marcobrico.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        Long invoiceId,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String reference,
        LocalDateTime createdAt
) {}
