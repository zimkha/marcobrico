package com.mmd.marcobrico.specification;

import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.domain.SaleItem;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class SaleSpecification {
    public static Specification<Sale> withFilters(
            Long productId,
            Long userId,
            Boolean canceled,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            if (canceled != null) {
                predicates.add(cb.equal(root.get("canceled"), canceled));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            if (productId != null) {
                Join<Sale, SaleItem> items = root.join("items");
                predicates.add(cb.equal(items.get("product").get("id"), productId));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
