package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "invoices", uniqueConstraints = {
        @UniqueConstraint(columnNames = "invoiceNumber")
})
@Getter
public class Invoice {
    /* Getters */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @OneToOne(optional = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    protected Invoice() {}

    private Invoice(String invoiceNumber, Sale sale, BigDecimal totalAmount) {
        this.invoiceNumber = invoiceNumber;
        this.sale = sale;
        this.totalAmount = totalAmount;
        this.paidAmount = BigDecimal.ZERO;
        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
    }

    public static Invoice issue(String invoiceNumber, Sale sale) {
        return new Invoice(invoiceNumber, sale, sale.getTotal());
    }

    public Invoice registerPayment(BigDecimal amount) {
        BigDecimal newPaid = this.paidAmount.add(amount);

        Invoice i = new Invoice(this.invoiceNumber, this.sale, this.totalAmount);
        i.paidAmount = newPaid;
        i.status = newPaid.compareTo(totalAmount) >= 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID;
        return i;
    }

    public Invoice cancel() {
        Invoice i = new Invoice(this.invoiceNumber, this.sale, this.totalAmount);
        i.status = InvoiceStatus.CANCELED;
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice i)) return false;
        return Objects.equals(id, i.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
