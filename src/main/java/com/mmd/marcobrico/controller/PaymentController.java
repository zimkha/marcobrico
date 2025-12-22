package com.mmd.marcobrico.controller;

import com.mmd.marcobrico.dto.payment.PaymentCreateDto;
import com.mmd.marcobrico.dto.payment.PaymentResponseDto;
import com.mmd.marcobrico.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public PaymentResponseDto pay(@RequestBody @Valid PaymentCreateDto dto) {
        return service.pay(dto);
    }

    @PostMapping("/{id}/refund")
    public PaymentResponseDto refund(@PathVariable Long id) {
        return service.refund(id);
    }

    @GetMapping("/invoice/{invoiceId}")
    public List<PaymentResponseDto> byInvoice(@PathVariable Long invoiceId) {
        return service.listByInvoice(invoiceId);
    }
}
