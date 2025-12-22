package com.mmd.marcobrico.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "partners")
public class Partner {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType type;

    @Column(unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    private String address;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Partner() {}

    private Partner(String name,
                    PartnerType type,
                    String phone,
                    String email,
                    String address) {

        this.name = name;
        this.type = type;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    public static Partner create(String name,
                                 PartnerType type,
                                 String phone,
                                 String email,
                                 String address) {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nom partenaire obligatoire");

        if (type == null)
            throw new IllegalArgumentException("Type partenaire obligatoire");

        return new Partner(name, type, phone, email, address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partner p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

