package com.mmd.marcobrico.domain;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private boolean canceled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = true)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    protected Sale() {}

    private Sale(User user, List<SaleItem> items, BigDecimal total, Client client) {
        this.user = user;
        this.items = List.copyOf(items);
        this.total = total;
        this.canceled = false;
        this.client = client;
        this.createdAt = LocalDateTime.now();
    }
    private Sale(
            User user,
            List<SaleItem> items,
            BigDecimal total,
            Client client,
            Delivery delivery
    ) {
        this.user = user;
        this.items = List.copyOf(items);
        this.total = total;
        this.client = client;
        this.delivery = delivery;
        this.canceled = false;
        this.createdAt = LocalDateTime.now();
    }


    public static Sale create(User user, List<SaleItem> items, Client client) {
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var sale = new Sale(user, items, total, client);
        items.forEach(i -> i.attachToSale(sale));
        return sale;
    }

    public static Sale createSimple(User user, List<SaleItem> items) {
        return createInternal(user, items, null, null);
    }
    private static Sale createInternal(
            User user,
            List<SaleItem> items,
            Client client,
            Delivery delivery
    ) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Une vente doit contenir au moins un article");

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Sale sale = new Sale(user, items, total, client, delivery);
        items.forEach(i -> i.attachToSale(sale));
        return sale;
    }

    public void cancel() {
        if (this.canceled) {
            throw new IllegalStateException("Vente déjà annulée");
        }
        this.canceled = true;
    }

    public static Sale createFromDelivery(
            User user,
            List<SaleItem> items,
            Client client,
            Delivery delivery
    ) {
        if (client == null)
            throw new IllegalArgumentException("Client obligatoire pour une vente issue d’une livraison");

        if (delivery == null)
            throw new IllegalArgumentException("Delivery obligatoire pour une vente issue d’une livraison");

        return createInternal(user, items, client, delivery);
    }

    private void recalculateTotal() {
        this.total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private Sale(User user, List<SaleItem> items, BigDecimal total, boolean canceled) {
        this.user = user;
        this.items = List.copyOf(items);
        this.total = total;
        this.canceled = canceled;
        this.createdAt = LocalDateTime.now();
    }

}
