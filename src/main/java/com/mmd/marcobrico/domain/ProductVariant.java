package com.mmd.marcobrico.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType saleUnit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal conversionToBaseUnit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal stock;

    @Column(nullable = false)
    private boolean wholesale;

    /**
     * CONCEPT: Validation métier dans l'entité
     * Alternative: Bean Validation (@Min, @Positive)
     */
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        if (stock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Stock négatif interdit");
        }
        if (conversionToBaseUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Facteur de conversion doit être > 0");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Prix négatif interdit");
        }
    }

    /**
     * Méthodes métier pour gérer le stock
     * CONCEPT: Domain-Driven Design - la logique métier reste dans le domaine
     */
    public void addStock(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantité doit être positive");
        }
        this.stock = this.stock.add(quantity);
    }

    public void removeStock(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantité doit être positive");
        }
        if (this.stock.compareTo(quantity) < 0) {
            throw new IllegalStateException("Stock insuffisant");
        }
        this.stock = this.stock.subtract(quantity);
    }

    public void updateFrom(UnitType saleUnit, BigDecimal conversionFactor,
                           BigDecimal price, Boolean wholesale) {
        if (saleUnit != null) {
            this.saleUnit = saleUnit;
        }
        if (conversionFactor != null) {
            this.conversionToBaseUnit = conversionFactor;
        }
        if (price != null) {
            this.price = price;
        }
        if (wholesale != null) {
            this.wholesale = wholesale;
        }
    }
}

