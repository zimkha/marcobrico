package com.mmd.marcobrico.specification;

import com.mmd.marcobrico.domain.InventoryEntry;
import com.mmd.marcobrico.domain.InventoryType;
import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.dto.inventory.InventoryFilterDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InventorySpecification {
    public static Specification<InventoryEntry> filter(InventoryFilterDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dto.productName() != null && !dto.productName().isBlank()) {
                Join<InventoryEntry, Product> productJoin = root.join("product");
                predicates.add(
                        cb.like(
                                cb.lower(productJoin.get("name")),
                                "%" + dto.productName().toLowerCase() + "%"
                        )
                );
            }

            if (dto.type() != null) {
                predicates.add(cb.equal(root.get("type"), InventoryType.valueOf(dto.type())));
            }

            if (dto.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), dto.userId()));
            }

            if (dto.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dto.startDate()));
            }

            if (dto.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dto.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
