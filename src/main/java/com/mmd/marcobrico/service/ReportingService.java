package com.mmd.marcobrico.service;


import com.mmd.marcobrico.dto.reporting.RevenueByPeriodDto;
import com.mmd.marcobrico.dto.reporting.RevenueDto;
import com.mmd.marcobrico.dto.reporting.UnpaidByClientDto;
import com.mmd.marcobrico.dto.reporting.UnpaidInvoiceDto;
import com.mmd.marcobrico.repository.ReportingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingService {

    private final ReportingRepository repository;

    public RevenueDto totalRevenue() {
        return repository.totalRevenue();
    }

    public List<RevenueByPeriodDto> revenueByDay(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return repository.revenueByDay(start, end)
                .stream()
                .map(p -> new RevenueByPeriodDto(p.getPeriod(), p.getRevenue()))
                .toList();
    }

    public List<UnpaidInvoiceDto> unpaidInvoices() {
        return repository.unpaidInvoices();
    }

    public List<UnpaidByClientDto> unpaidByClient() {
        return repository.unpaidByClient();
    }
}
