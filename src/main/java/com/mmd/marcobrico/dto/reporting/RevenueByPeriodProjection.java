package com.mmd.marcobrico.dto.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueByPeriodProjection {
    LocalDate getPeriod();
    BigDecimal getRevenue();
}
