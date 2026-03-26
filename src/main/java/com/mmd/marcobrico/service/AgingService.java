package com.mmd.marcobrico.service;


import com.mmd.marcobrico.dto.aging.AgingBucketDto;
import com.mmd.marcobrico.dto.aging.AgingReportDto;
import com.mmd.marcobrico.repository.AgingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgingService {

    private final AgingRepository repository;
    public AgingReportDto agingUnpaid() {

        List<AgingBucketDto> buckets = repository.agingBuckets();

        BigDecimal total = buckets.stream()
                .map(AgingBucketDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AgingReportDto(total, buckets);
    }
}
