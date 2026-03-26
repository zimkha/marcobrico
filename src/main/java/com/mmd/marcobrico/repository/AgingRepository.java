package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Invoice;
import com.mmd.marcobrico.dto.aging.AgingBucketDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;


import java.util.List;



public interface AgingRepository extends JpaRepository<Invoice, Long> {

    default Invoice getById(Long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException(""));
    }
    @Query("""
SELECT new com.mmd.marcobrico.dto.aging.AgingBucketDto(
    CASE
        WHEN CURRENT_DATE - i.issuedAt <= 30 THEN '0-30'
        WHEN CURRENT_DATE - i.issuedAt <= 60 THEN '31-60'
        WHEN CURRENT_DATE - i.issuedAt <= 90 THEN '61-90'
        ELSE '+90'
    END,
    SUM(i.totalAmount - i.paidAmount)
)
FROM Invoice i
WHERE i.status <> 'PAID'
  AND i.status <> 'CANCELED'
GROUP BY
    CASE
        WHEN CURRENT_DATE - i.issuedAt <= 30 THEN '0-30'
        WHEN CURRENT_DATE - i.issuedAt <= 60 THEN '31-60'
        WHEN CURRENT_DATE - i.issuedAt <= 90 THEN '61-90'
        ELSE '+90'
    END
""")
    List<AgingBucketDto> agingBuckets();
}
