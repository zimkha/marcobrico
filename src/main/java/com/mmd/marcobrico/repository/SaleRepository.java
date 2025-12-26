package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
    @Query("""
        SELECT COUNT(si) 
        FROM SaleItem si 
        WHERE si.product.id = :productId
    """)
    long countByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT si.sale 
        FROM SaleItem si 
        WHERE si.product.id = :productId 
        ORDER BY si.sale.createdAt DESC
    """)
    List<Sale> findSalesByProductIdOrderByDateDesc(@Param("productId") Long productId);

    @Query("""
        SELECT SUM(si.price * si.quantity)
        FROM SaleItem si
        WHERE si.product.id = :productId
    """)
    BigDecimal getRevenueByProductId(@Param("productId") Long productId);
}

