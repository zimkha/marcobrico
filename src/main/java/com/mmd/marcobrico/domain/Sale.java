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
    @JoinColumn(name = "sale_id")
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

    @ManyToOne
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

    public static Sale create(User user, List<SaleItem> items, Client client) {
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var sale = new Sale(user, items, total, client);
        items.forEach(i -> i.attachToSale(sale));
        return sale;
    }


    public void cancel() {
        if (this.canceled) {
            throw new IllegalStateException("Vente déjà annulée");
        }
        this.canceled = true;
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
