package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.reporting.RevenueByPeriodDto;
import com.mmd.marcobrico.dto.reporting.RevenueDto;
import com.mmd.marcobrico.dto.reporting.UnpaidByClientDto;
import com.mmd.marcobrico.dto.reporting.UnpaidInvoiceDto;
import com.mmd.marcobrico.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reporting")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService service;

    @GetMapping("/revenue/total")
    public RevenueDto totalRevenue() {
        return service.totalRevenue();
    }

    @GetMapping("/revenue/daily")
    public List<RevenueByPeriodDto> revenueByDay(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return service.revenueByDay(start, end);
    }

    @GetMapping("/unpaid/invoices")
    public List<UnpaidInvoiceDto> unpaidInvoices() {
        return service.unpaidInvoices();
    }

    @GetMapping("/unpaid/clients")
    public List<UnpaidByClientDto> unpaidByClient() {
        return service.unpaidByClient();
    }
}
