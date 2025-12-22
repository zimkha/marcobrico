package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "deliveries")
@Getter
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Sale sale;

    @ManyToOne(optional = false)
    private Partner carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private String trackingNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Delivery() {}

    private Delivery(Sale sale, Partner carrier) {
        this.sale = sale;
        this.carrier = carrier;
        this.status = DeliveryStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public static Delivery create(Sale sale, Partner carrier) {

        if (carrier.getType() != PartnerType.CARRIER)
            throw new IllegalArgumentException("Partenaire non transporteur");

        if (sale.isCanceled())
            throw new IllegalArgumentException("Vente annulée");

        return new Delivery(sale, carrier);
    }

    public Delivery markInTransit(String tracking) {
        if (this.status != DeliveryStatus.CREATED)
            throw new IllegalStateException("Livraison non expédiable");

        Delivery d = new Delivery(this.sale, this.carrier);
        d.status = DeliveryStatus.IN_TRANSIT;
        d.trackingNumber = tracking;
        return d;
    }

    public Delivery markDelivered() {
        if (this.status != DeliveryStatus.IN_TRANSIT)
            throw new IllegalStateException("Livraison non livrable");

        Delivery d = new Delivery(this.sale, this.carrier);
        d.status = DeliveryStatus.DELIVERED;
        d.trackingNumber = this.trackingNumber;
        return d;
    }

    public Delivery cancel() {
        if (this.status == DeliveryStatus.DELIVERED)
            throw new IllegalStateException("Livraison déjà effectuée");

        Delivery d = new Delivery(this.sale, this.carrier);
        d.status = DeliveryStatus.CANCELED;
        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Delivery d)) return false;
        return Objects.equals(id, d.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
