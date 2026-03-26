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

    Page<Product> findByActiveTrue(Pageable pageable);


    /**
     * CONCEPT: Query Method - Filtrage par catégorie
     *
     * Generated SQL:
     * SELECT * FROM products WHERE category_id = ?
     *
     * @param categoryId ID de la catégorie
     * @param pageable pagination/tri
     * @return Page de produits
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * CONCEPT: Query Method - LIKE insensible à la casse
     *
     * Generated SQL:
     * SELECT * FROM products
     * WHERE LOWER(name) LIKE LOWER('%' || ? || '%')
     *
     * @param name fragment de nom à chercher
     * @param pageable pagination
     * @return Page de produits correspondants
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);



    /**
     * CONCEPT: Existence Check (optimisation)
     *
     * Generated SQL:
     * SELECT COUNT(*) > 0 FROM products WHERE reference = ?
     *
     * Plus performant que findByReference().isPresent()
     * car ne charge pas toute l'entité
     *
     * @param reference référence à vérifier
     * @return true si existe, false sinon
     */
    boolean existsByReference(String reference);



}