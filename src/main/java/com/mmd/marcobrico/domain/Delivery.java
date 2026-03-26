package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "deliveries")
@Getter
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryItem> items = new ArrayList<>();

    private String address;
    private String trackingNumber;

    private LocalDateTime createdAt;

    protected Delivery() {}

    private Delivery(Client client, String address) {
        this.client = client;
        this.address = address;
        this.status = DeliveryStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public static Delivery create(Client client, String address) {
        return new Delivery(client, address);
    }

    public void addItem(DeliveryItem item) {
        if (status != DeliveryStatus.CREATED)
            throw new IllegalStateException("Impossible d’ajouter des articles");
        this.items.add(item);
    }

    public void markInTransit(String tracking) {
        if (status != DeliveryStatus.CREATED)
            throw new IllegalStateException("Livraison non expédiable");
        this.status = DeliveryStatus.IN_TRANSIT;
        this.trackingNumber = tracking;
    }

    public void markDelivered() {
        if (status != DeliveryStatus.IN_TRANSIT)
            throw new IllegalStateException("Livraison non livrable");
        this.status = DeliveryStatus.DELIVERED;
    }



    public void cancel() {
        if (status == DeliveryStatus.DELIVERED)
            throw new IllegalStateException("Livraison déjà effectuée");
        this.status = DeliveryStatus.CANCELED;
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


