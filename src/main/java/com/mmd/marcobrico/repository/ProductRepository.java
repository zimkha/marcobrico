package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.dto.reporting.AverageSoldDto;
import com.mmd.marcobrico.dto.reporting.TopProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByReference(String reference);

    boolean existsByReference(String reference);
    @Query("""
    SELECT new com.mmd.marcobrico.dto.reporting.TopProductDto(
        p.id, p.name, SUM(i.quantity)
    )
        FROM SaleItem i
        JOIN i.product p
        JOIN i.sale s
        WHERE s.canceled = false
        GROUP BY p.id, p.name
        ORDER BY SUM(i.quantity) DESC
    """)
    Page<TopProductDto> topProducts(Pageable pageable);
    @Query("""
        SELECT new com.mmd.marcobrico.dto.reporting.AverageSoldDto(
            p.id, p.name, AVG(i.quantity)
        )
        FROM SaleItem i
        JOIN i.product p
        JOIN i.sale s
        WHERE s.canceled = false
        GROUP BY p.id, p.name
    """)
    List<AverageSoldDto> averageSold();
    List<Product> findTop10ByNameContainingIgnoreCaseOrderByNameAsc(String name);



}