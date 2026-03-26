package com.mmd.marcobrico.dto.reporting;

import java.math.BigDecimal;

public record UnpaidByClientDto(
        Long clientId,
        String clientName,
        BigDecimal unpaidAmount
) {}
