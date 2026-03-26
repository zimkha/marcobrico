package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.invoice.InvoiceResponseDto;

import java.math.BigDecimal;

public interface InvoiceService {

    InvoiceResponseDto issue(Long saleId);

    InvoiceResponseDto pay(Long invoiceId, BigDecimal amount);

    byte[] generatePdf(Long invoiceId);
}
