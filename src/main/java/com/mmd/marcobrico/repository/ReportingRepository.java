package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Payment;
import com.mmd.marcobrico.dto.reporting.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;



import java.time.LocalDateTime;
import java.util.List;

public interface ReportingRepository  extends Repository<Payment, Long> {
    // 🔹 CA global
    @Query("""
        SELECT new com.mmd.marcobrico.dto.reporting.RevenueDto(
            COALESCE(SUM(p.amount), 0)
        )
        FROM Payment p
        WHERE p.status = 'SUCCESS'
    """)
    RevenueDto totalRevenue();


    @Query("""
    SELECT 
        DATE(p.createdAt) AS period,
        SUM(p.amount) AS revenue
    FROM Payment p
    WHERE p.status = 'SUCCESS'
      AND p.createdAt BETWEEN :start AND :end
    GROUP BY DATE(p.createdAt)
    ORDER BY DATE(p.createdAt)
""")
    List<RevenueByPeriodProjection> revenueByDay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
        SELECT new com.mmd.marcobrico.dto.reporting.UnpaidInvoiceDto(
            i.id,
            i.invoiceNumber,
            c.name,
            i.totalAmount,
            i.paidAmount,
            (i.totalAmount - i.paidAmount),
            i.issuedAt
        )
        FROM Invoice i
        JOIN i.sale s
        JOIN s.client c
        WHERE i.status <> 'PAID'
          AND i.status <> 'CANCELED'
    """)
    List<UnpaidInvoiceDto> unpaidInvoices();

    @Query("""
        SELECT new com.mmd.marcobrico.dto.reporting.UnpaidByClientDto(
            c.id,
            c.name,
            SUM(i.totalAmount - i.paidAmount)
        )
        FROM Invoice i
        JOIN i.sale s
        JOIN s.client c
        WHERE i.status <> 'PAID'
          AND i.status <> 'CANCELED'
        GROUP BY c.id, c.name
    """)
    List<UnpaidByClientDto> unpaidByClient();
}
