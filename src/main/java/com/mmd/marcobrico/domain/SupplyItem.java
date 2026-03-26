package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "supply_items")
@Getter
public class SupplyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Supply supply;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private BigDecimal total;

    protected SupplyItem() {}

    private SupplyItem(Product product, int quantity, BigDecimal purchasePrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantité invalide");
        this.product = product;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.total = purchasePrice.multiply(BigDecimal.valueOf(quantity));
    }

    public static SupplyItem create(Product product, int quantity, BigDecimal purchasePrice) {
        return new SupplyItem(product, quantity, purchasePrice);
    }

    void attachToSupply(Supply supply) {
        this.supply = supply;
    }
}

