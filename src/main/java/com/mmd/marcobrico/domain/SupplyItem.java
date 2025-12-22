package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "supply_items")
@Getter
public class SupplyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    protected SupplyItem() {}

    private SupplyItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public static SupplyItem create(Product product, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantité invalide");

        return new SupplyItem(product, quantity);
    }
}
