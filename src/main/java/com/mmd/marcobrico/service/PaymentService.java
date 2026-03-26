package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.payment.PaymentCreateDto;
import com.mmd.marcobrico.dto.payment.PaymentResponseDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto pay(PaymentCreateDto dto);

    PaymentResponseDto refund(Long paymentId);

    List<PaymentResponseDto> listByInvoice(Long invoiceId);
}
