package com.mmd.marcobrico.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "payments")
public class Payment {

    /* Getters */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Payment() {}

    private Payment(
            Invoice invoice,
            PaymentMethod method,
            BigDecimal amount,
            PaymentStatus status,
            String reference
    ) {
        this.invoice = invoice;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.reference = reference;
        this.createdAt = LocalDateTime.now();
    }

    public static Payment create(
            Invoice invoice,
            PaymentMethod method,
            BigDecimal amount,
            String reference
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Montant invalide");

        return new Payment(
                invoice,
                method,
                amount,
                PaymentStatus.PENDING,
                reference
        );
    }

    public Payment markSuccess() {
        return new Payment(invoice, method, amount, PaymentStatus.SUCCESS, reference);
    }

    public Payment markFailed() {
        return new Payment(invoice, method, amount, PaymentStatus.FAILED, reference);
    }

    public Payment refund() {
        return new Payment(invoice, method, amount.negate(), PaymentStatus.REFUNDED, reference);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
