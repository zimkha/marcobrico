package com.mmd.marcobrico.utils;

import com.mmd.marcobrico.domain.InvoiceStatus;
import com.mmd.marcobrico.domain.Payment;
import com.mmd.marcobrico.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public class InvoicePaymentCalculator {
    public static BigDecimal totalPaid(List<Payment> payments) {
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static InvoiceStatus computeStatus(
            BigDecimal total,
            BigDecimal paid
    ) {
        if (paid.compareTo(BigDecimal.ZERO) == 0)
            return InvoiceStatus.ISSUED;

        if (paid.compareTo(total) < 0)
            return InvoiceStatus.PARTIALLY_PAID;

        return InvoiceStatus.PAID;
    }
}
