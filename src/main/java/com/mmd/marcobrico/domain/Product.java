package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer seuilStock;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private boolean active;

    protected Product() {}

    private Product(Long id, String name, String reference, BigDecimal price,
                    Integer quantity, Integer seuilStock, Category category, boolean active) {
        this.id = id;
        this.name = name;
        this.reference = reference;
        this.price = price;
        this.quantity = quantity;
        this.seuilStock = seuilStock;
        this.category = category;
        this.active = active;
    }


    public static Product create(String name, String reference, BigDecimal price,
                                 Integer quantity, Integer seuilStock, Category category) {
        if (quantity < 0) throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        return new Product(null, name, reference, price, quantity, seuilStock, category, true);
    }

    public Product update(String name, BigDecimal price, Integer seuilStock, Category category) {
        return new Product(id, name, reference, price, quantity, seuilStock, category, active);
    }

    public Product changeQuantity(int newQuantity) {
        if (newQuantity < 0) throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        return new Product(id, name, reference, price, newQuantity, seuilStock, category, active);
    }

    public Product addQuantity(int delta) {
        int updatedQuantity = this.quantity + delta;
        if (updatedQuantity < 0)
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        return new Product(id, name, reference, price, updatedQuantity, seuilStock, category, active);
    }

}
