package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.aging.AgingReportDto;
import com.mmd.marcobrico.service.AgingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aging")
public class AgingController {

    private final AgingService service;

    @GetMapping("/unpaid/aging")
    public AgingReportDto aging() {
        return service.agingUnpaid();
    }
}
