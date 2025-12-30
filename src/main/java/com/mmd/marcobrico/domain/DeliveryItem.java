package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_items")
@Getter
public class DeliveryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    Product product;

    @ManyToOne(optional = false)
    private Delivery delivery;

    @Column(nullable = false)
    int quantityDelivered;

    @Column(nullable = false)
    BigDecimal salePrice;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected DeliveryItem(){}
    private DeliveryItem(Delivery delivery, Product product, int quantityDelivered) {
        this.delivery = delivery;
        this.product = product;
        this.quantityDelivered = quantityDelivered;
    }

    public static DeliveryItem create(Delivery delivery, Product product, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantité livrée invalide");
        return new DeliveryItem(delivery, product, quantity);
    }

}
