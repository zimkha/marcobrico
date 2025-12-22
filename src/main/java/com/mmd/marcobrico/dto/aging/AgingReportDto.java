package com.mmd.marcobrico.dto.aging;

import java.math.BigDecimal;
import java.util.List;

public record AgingReportDto(
        BigDecimal totalUnpaid,
        List<AgingBucketDto> buckets
) {}
