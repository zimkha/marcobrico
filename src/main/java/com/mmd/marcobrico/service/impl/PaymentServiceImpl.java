package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Invoice;
import com.mmd.marcobrico.domain.Payment;
import com.mmd.marcobrico.dto.payment.PaymentCreateDto;
import com.mmd.marcobrico.dto.payment.PaymentResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.mapper.PaymentMapper;
import com.mmd.marcobrico.repository.InvoiceRepository;
import com.mmd.marcobrico.repository.PaymentRepository;
import com.mmd.marcobrico.service.DashboardEventService;
import com.mmd.marcobrico.service.PaymentService;
import com.mmd.marcobrico.utils.InvoicePaymentCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper mapper;
    private final DashboardEventService dashboardEventService;

    @Override
    public PaymentResponseDto pay(PaymentCreateDto dto) {

        Invoice invoice = invoiceRepository.findById(dto.invoiceId())
                .orElseThrow(() -> new BusinessException("Facture introuvable"));

        Payment payment = Payment.create(
                invoice,
                dto.method(),
                dto.amount(),
                dto.reference()
        );

        // ici on simule le succès (plus tard gateway externe)
        Payment success = payment.markSuccess();
        paymentRepository.save(success);
        dashboardEventService.publish(
                Map.of(
                        "type", "PAYMENT",
                        "amount", payment.getAmount(),
                        "invoiceId", payment.getInvoice().getId()
                )
        );


        // recalcul facture
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        BigDecimal paid = InvoicePaymentCalculator.totalPaid(payments);

        Invoice updated = invoice.registerPayment(paid.subtract(invoice.getPaidAmount()));
        invoiceRepository.save(updated);

        return mapper.toDto(success);
    }

    @Override
    public PaymentResponseDto refund(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Paiement introuvable"));

        Payment refund = payment.refund();
        paymentRepository.save(refund);

        return mapper.toDto(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> listByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
