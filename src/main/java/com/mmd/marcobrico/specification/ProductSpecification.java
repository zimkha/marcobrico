package com.mmd.marcobrico.specification;

import com.mmd.marcobrico.domain.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(String name, Long categoryId, Boolean belowThreshold) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (belowThreshold != null && belowThreshold) {
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), root.get("seuilStock")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
