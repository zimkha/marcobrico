package com.mmd.marcobrico.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Getter
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal price;

    protected SaleItem() {}

    private SaleItem(Product product, int quantity, BigDecimal price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public static SaleItem create(Product product, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantité invalide");
        return new SaleItem(product, quantity, product.getPrice());
    }
}
