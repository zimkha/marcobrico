package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * CONCEPT CLÉ: Repository avec requêtes personnalisées
 *
 * Mélanges de:
 * - Query Methods (générés automatiquement)
 * - @Query (JPQL ou SQL natif)
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * CONCEPT: Query Method - Relation ManyToOne
     *
     * Generated SQL:
     * SELECT v.* FROM product_variants v
     * WHERE v.product_id = ?
     *
     * Spring comprend "Product" comme la relation @ManyToOne
     *
     * @param productId ID du produit parent
     * @return Liste des variantes du produit
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * CONCEPT: Query Method - Filtrage booléen
     *
     * Generated SQL:
     * SELECT * FROM product_variants WHERE wholesale = ?
     *
     * @param wholesale true pour gros, false pour détail
     * @param pageable pagination
     * @return Page de variantes filtrées
     */
    Page<ProductVariant> findByWholesale(boolean wholesale, Pageable pageable);

    /**
     * CONCEPT: Query Method - Comparaison BigDecimal
     *
     * Generated SQL:
     * SELECT * FROM product_variants WHERE stock = ?
     *
     * Utile pour trouver les ruptures de stock (stock = 0)
     *
     * @param stock valeur exacte du stock
     * @param pageable pagination
     * @return Page de variantes avec ce stock
     */
    Page<ProductVariant> findByStock(BigDecimal stock, Pageable pageable);

    /**
     * CONCEPT: Query Method - Comparaison avec LessThan
     *
     * Generated SQL:
     * SELECT * FROM product_variants WHERE stock < ?
     *
     * Naming conventions pour comparaisons:
     * - LessThan → <
     * - LessThanEqual → <=
     * - GreaterThan → >
     * - GreaterThanEqual → >=
     * - Between → BETWEEN x AND y
     *
     * @param threshold seuil minimal
     * @param pageable pagination
     * @return Page de variantes sous le seuil
     */
    Page<ProductVariant> findByStockLessThan(BigDecimal threshold, Pageable pageable);

    /**
     * CONCEPT: @Query - Requête JPQL personnalisée
     *
     * Quand utiliser @Query?
     * - Requête complexe (JOIN, sous-requêtes)
     * - Agrégations (SUM, AVG, COUNT...)
     * - Performance (fetch join pour éviter N+1)
     *
     * JPQL vs SQL:
     * - JPQL = langage orienté objet (noms d'entités)
     * - SQL = langage relationnel (noms de tables)
     *
     * Exemple ici: JOIN FETCH pour charger Product en une requête
     * Évite le problème N+1 (une requête par variant)
     */
    @Query("SELECT v FROM ProductVariant v " +
            "JOIN FETCH v.product p " +
            "WHERE p.id = :productId")
    List<ProductVariant> findByProductIdWithProduct(@Param("productId") Long productId);

    /**
     * CONCEPT: @Query avec agrégation
     *
     * Calcule la valeur totale du stock d'un produit
     * SUM(stock * price) de toutes ses variantes
     *
     * @param productId ID du produit
     * @return valeur totale (ou 0 si aucune variante)
     */
    @Query("SELECT COALESCE(SUM(v.stock * v.price), 0) " +
            "FROM ProductVariant v " +
            "WHERE v.product.id = :productId")
    BigDecimal calculateTotalStockValue(@Param("productId") Long productId);

    /**
     * CONCEPT: @Query - Comptage
     *
     * Compte les variantes avec stock disponible pour un produit
     *
     * @param productId ID du produit
     * @return nombre de variantes en stock
     */
    @Query("SELECT COUNT(v) FROM ProductVariant v " +
            "WHERE v.product.id = :productId AND v.stock > 0")
    long countAvailableVariants(@Param("productId") Long productId);

    /**
     * CONCEPT: @Query - Filtrage multiple
     *
     * Recherche variantes par plusieurs critères
     * WHERE avec AND/OR
     *
     * @param productId ID du produit (obligatoire)
     * @param wholesale type de vente (optionnel)
     * @param minPrice prix minimum (optionnel)
     * @return Liste de variantes correspondantes
     */
    @Query("SELECT v FROM ProductVariant v " +
            "WHERE v.product.id = :productId " +
            "AND (:wholesale IS NULL OR v.wholesale = :wholesale) " +
            "AND (:minPrice IS NULL OR v.price >= :minPrice)")
    List<ProductVariant> searchVariants(
            @Param("productId") Long productId,
            @Param("wholesale") Boolean wholesale,
            @Param("minPrice") BigDecimal minPrice);

    /**
     * CONCEPT: SQL Natif avec @Query(nativeQuery = true)
     *
     * Quand utiliser SQL natif?
     * - Fonctions spécifiques à la DB (PostgreSQL, MySQL...)
     * - Performance critique
     * - Requêtes legacy à migrer
     *
     * Inconvénient: moins portable entre DB
     */
    @Query(value = "SELECT * FROM product_variants v " +
            "WHERE v.product_id = :productId " +
            "AND v.stock BETWEEN :minStock AND :maxStock",
            nativeQuery = true)
    List<ProductVariant> findByProductAndStockRange(
            @Param("productId") Long productId,
            @Param("minStock") BigDecimal minStock,
            @Param("maxStock") BigDecimal maxStock);

    /**
     * CONCEPT: Query Method - Ordre de tri personnalisé
     *
     * Generated SQL:
     * SELECT * FROM product_variants
     * WHERE product_id = ?
     * ORDER BY price DESC
     *
     * Naming: OrderBy + PropertyName + Asc/Desc
     */
    List<ProductVariant> findByProductIdOrderByPriceDesc(Long productId);
}