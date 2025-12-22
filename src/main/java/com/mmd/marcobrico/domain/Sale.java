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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sale_id")
    private List<SaleItem> items;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private boolean canceled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    protected Sale() {}

    private Sale(User user, List<SaleItem> items, BigDecimal total) {
        this.user = user;
        this.items = List.copyOf(items);
        this.total = total;
        this.canceled = false;
        this.createdAt = LocalDateTime.now();
    }

    public static Sale create(User user, List<SaleItem> items) {
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Sale(user, items, total);
    }

    public Sale cancel() {
        return new Sale(this.user, this.items, this.total, true);
    }

    private Sale(User user, List<SaleItem> items, BigDecimal total, boolean canceled) {
        this.user = user;
        this.items = List.copyOf(items);
        this.total = total;
        this.canceled = canceled;
        this.createdAt = LocalDateTime.now();
    }

}
