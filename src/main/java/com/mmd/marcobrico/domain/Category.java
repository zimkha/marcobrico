package com.mmd.marcobrico.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "categories")
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Category() {

    }

    private Category(Long id, String name, String description,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public static Category create(String name, String description) {
        return new Category(
                null,
                name,
                description,
                LocalDateTime.now(),
                null
        );
    }

    public Category update(String name, String description) {
        return new Category(
                this.id,
                name,
                description,
                this.createdAt,
                LocalDateTime.now()
        );
    }

}
