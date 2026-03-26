package com.mmd.marcobrico.specification;

import com.mmd.marcobrico.domain.Partner;
import com.mmd.marcobrico.domain.PartnerType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PartnerSpecification {
    public static Specification<Partner> search(
            String name,
            PartnerType type,
            String phone,
            String email
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));

            if (type != null)
                predicates.add(cb.equal(root.get("type"), type));

            if (phone != null)
                predicates.add(cb.equal(root.get("phone"), phone));

            if (email != null)
                predicates.add(cb.equal(root.get("email"), email));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
