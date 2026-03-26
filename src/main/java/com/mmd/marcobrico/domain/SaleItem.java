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
    private Sale sale;

    @Column(nullable = false)
    private int quantity;


    @Column(nullable = false)
    private BigDecimal price;

    protected SaleItem() {}

    private SaleItem(Product product, int quantity, BigDecimal price) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantité invalide");
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public static SaleItem create(Product product, int quantity, BigDecimal salePrice) {
        return new SaleItem(product, quantity, salePrice);
    }

    void attachToSale(Sale sale) {
        this.sale = sale;
    }
}
