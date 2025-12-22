package com.mmd.marcobrico.service.impl;


import com.mmd.marcobrico.domain.Invoice;
import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.dto.invoice.InvoiceResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.mapper.InvoiceMapper;
import com.mmd.marcobrico.repository.InvoiceRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import com.mmd.marcobrico.service.DashboardEventService;
import com.mmd.marcobrico.service.InvoiceNumberGenerator;
import com.mmd.marcobrico.service.InvoicePdfGenerator;
import com.mmd.marcobrico.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;
    private final InvoiceMapper mapper;
    private final InvoiceNumberGenerator numberGenerator;
    private final InvoicePdfGenerator pdfGenerator;
    private final DashboardEventService dashboardEventService;

    @Override
    public InvoiceResponseDto issue(Long saleId) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new BusinessException("Vente introuvable"));

        Invoice invoice = Invoice.issue(numberGenerator.generate(), sale);
        return mapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponseDto pay(Long invoiceId, BigDecimal amount) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Facture introuvable"));

        Invoice paid = invoice.registerPayment(amount);
        dashboardEventService.publish(
                Map.of(
                        "type", "INVOICE",
                        "invoiceNumber", invoice.getInvoiceNumber(),
                        "total", invoice.getTotalAmount()
                )
        );
        return mapper.toDto(invoiceRepository.save(paid));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Facture introuvable"));

        return pdfGenerator.generate(invoice);
    }
}
