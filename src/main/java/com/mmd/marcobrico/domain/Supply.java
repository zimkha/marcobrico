package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "supplies")
@Getter
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Partner supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplyStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "supply_id")
    private List<SupplyItem> items;

    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Supply() {}

    private Supply(Partner supplier, List<SupplyItem> items) {
        this.supplier = supplier;
        this.items = List.copyOf(items);
        this.status = SupplyStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    private Supply(Partner supplier, Delivery delivery, List<SupplyItem> items) {
        this.supplier = supplier;
        this.delivery = delivery;
        this.items = List.copyOf(items);
        this.status = SupplyStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public static Supply create(Partner supplier, List<SupplyItem> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Approvisionnement vide");

        return new Supply(supplier, items);
    }

    public static Supply createForDelivery(
            Partner supplier,
            Delivery delivery,
            List<SupplyItem> items
    ) {
        if (delivery == null)
            throw new IllegalArgumentException("Livraison requise");

        return new Supply(supplier, delivery, items);
    }

    public Supply markAsReceived() {
        if (this.status != SupplyStatus.CREATED)
            throw new IllegalStateException("Approvisionnement non recevable");

        Supply s = new Supply(this.supplier, this.items);
        s.status = SupplyStatus.RECEIVED;
        return s;
    }

    public Supply cancel() {
        if (this.status == SupplyStatus.RECEIVED)
            throw new IllegalStateException("Impossible d’annuler après réception");

        Supply s = new Supply(this.supplier, this.items);
        s.status = SupplyStatus.CANCELED;
        return s;
    }
}
