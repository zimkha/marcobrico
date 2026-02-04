package com.mmd.marcobrico.domain;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseUnit baseUnit;


    public void updateFrom(String name, BaseUnit baseUnit, Category category, Boolean active) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (baseUnit != null) {
            this.baseUnit = baseUnit;
        }
        if (category != null) {
            this.category = category;
        }
        if (active != null) {
            this.active = active;
        }
    }
}
