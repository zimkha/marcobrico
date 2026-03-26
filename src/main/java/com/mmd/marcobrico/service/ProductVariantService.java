package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.*;
import com.mmd.marcobrico.dto.product.ProductVariantCreateDto;
import com.mmd.marcobrico.dto.product.ProductVariantResponseDto;
import com.mmd.marcobrico.dto.product.ProductVariantUpdateDto;
import com.mmd.marcobrico.dto.product.StockAdjustmentDto;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * CONCEPT CLÉ: Service pour les variantes de produits
 *
 * Relation: ProductVariant APPARTIENT À Product
 * - Un variant existe toujours par rapport à un produit
 * - Certaines opérations nécessitent productId en paramètre
 */
public interface ProductVariantService {

    /**
     * Crée une variante pour un produit
     *
     * CONCEPT: Relation Parent-Enfant
     * - Le variant ne peut exister sans son product
     * - On vérifie que le product existe avant création
     *
     * @param productId ID du produit parent
     * @param dto données de la variante
     * @return la variante créée
     * @throws ResourceNotFoundException si product non trouvé
     * @throws IllegalArgumentException si conversionFactor <= 0
     */
    ProductVariantResponseDto create(Long productId, ProductVariantCreateDto dto);

    /**
     * Met à jour une variante
     *
     * CONCEPT: Update partiel
     * - Stock n'est PAS modifiable ici
     * - Utilisez addStock/removeStock pour le stock
     *
     * @param variantId ID de la variante
     * @param dto données de modification
     * @return la variante modifiée
     * @throws ResourceNotFoundException si variant non trouvé
     */
    ProductVariantResponseDto update(Long variantId, ProductVariantUpdateDto dto);

    /**
     * Récupère une variante par ID
     *
     * @param variantId ID de la variante
     * @return la variante
     * @throws ResourceNotFoundException si non trouvée
     */
    ProductVariantResponseDto findById(Long variantId);

    /**
     * Liste toutes les variantes d'un produit
     *
     * CONCEPT: Requête filtrée par relation
     * - Charge tous les variants du même product
     * - Utile pour afficher les options d'un produit
     *
     * @param productId ID du produit
     * @return liste des variantes du produit
     */
    List<ProductVariantResponseDto> findByProductId(Long productId);

    /**
     * Liste toutes les variantes (avec pagination)
     *
     * @param pageable pagination
     * @return page de variantes
     */
    Page<ProductVariantResponseDto> findAll(Pageable pageable);

    /**
     * Filtre par type de vente (wholesale ou retail)
     *
     * CONCEPT: Business Logic Filter
     * - wholesale = vente en gros
     * - retail (wholesale=false) = vente au détail
     *
     * @param wholesale true pour gros, false pour détail
     * @param pageable pagination
     * @return page de variantes filtrées
     */
    Page<ProductVariantResponseDto> findByWholesale(boolean wholesale, Pageable pageable);

    /**
     * Ajoute du stock à une variante
     *
     * CONCEPT: Domain Operation
     * - Opération métier spécifique
     * - Validation: quantité > 0
     * - Traçabilité: reason optionnel
     *
     * @param variantId ID de la variante
     * @param dto quantité et raison
     * @return la variante avec stock mis à jour
     * @throws ResourceNotFoundException si variant non trouvé
     * @throws IllegalArgumentException si quantité <= 0
     */
    ProductVariantResponseDto addStock(Long variantId, StockAdjustmentDto dto);

    /**
     * Retire du stock d'une variante
     *
     * CONCEPT: Business Validation
     * - Vérifie stock disponible
     * - Empêche stock négatif
     *
     * @param variantId ID de la variante
     * @param dto quantité et raison
     * @return la variante avec stock mis à jour
     * @throws ResourceNotFoundException si variant non trouvé
     * @throws IllegalArgumentException si quantité <= 0
     * @throws IllegalStateException si stock insuffisant
     */
    ProductVariantResponseDto removeStock(Long variantId, StockAdjustmentDto dto);

    /**
     * Supprime une variante
     *
     * ATTENTION: Peut échouer si variante dans commandes
     *
     * @param variantId ID de la variante
     * @throws DataIntegrityViolationException si contraintes FK
     */
    void delete(Long variantId);

    /**
     * Vérifie si un variant a du stock disponible
     *
     * CONCEPT: Query Method personnalisé
     * - Utile avant une vente
     * - Retourne booléen simple
     *
     * @param variantId ID de la variante
     * @param quantityNeeded quantité nécessaire
     * @return true si stock >= quantité, false sinon
     */
    boolean hasAvailableStock(Long variantId, java.math.BigDecimal quantityNeeded);

    /**
     * Liste les variantes en rupture de stock
     *
     * CONCEPT: Business Query
     * - stock = 0 OU stock < seuil minimal
     * - Utile pour réapprovisionnement
     *
     * @param pageable pagination
     * @return page de variantes en rupture
     */
    Page<ProductVariantResponseDto> findOutOfStock(Pageable pageable);
}