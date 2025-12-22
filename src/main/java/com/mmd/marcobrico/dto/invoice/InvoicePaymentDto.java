package com.mmd.marcobrico.dto.invoice;

import java.math.BigDecimal;

public record InvoicePaymentDto(
        BigDecimal amount
) {}
