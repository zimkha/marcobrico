package com.mmd.marcobrico.domain;

import com.mmd.marcobrico.domain.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "inventory_entries")
public class InventoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantityBefore;

    @Column(nullable = false)
    private int quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryType type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String comment;

    @ManyToOne User user;

    protected InventoryEntry() {}

    private InventoryEntry(Product product, int quantityBefore, int quantityAfter,
                           InventoryType type, String comment) {
        this.product = product;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.type = type;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public static InventoryEntry create(Product product, int quantityBefore, int quantityAfter,
                                        InventoryType type, String comment, User user) {
        if (quantityAfter < 0) throw new IllegalArgumentException("Stock ne peut pas être négatif");
        return new InventoryEntry(product, quantityBefore, quantityAfter, type, comment);
    }

}
