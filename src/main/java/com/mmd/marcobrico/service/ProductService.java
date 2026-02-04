package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.product.*;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    /**
     * Crée un nouveau produit
     *
     * @param dto données de création
     * @return le produit créé avec son ID
     * @throws IllegalArgumentException si categoryId invalide
     * @throws DataIntegrityViolationException si reference existe déjà
     */
    ProductResponseDto create(ProductCreateDto dto);

    /**
     * Met à jour un produit existant
     *
     * CONCEPT: Update partiel (PATCH)
     * - Seuls les champs non-null du DTO sont modifiés
     * - Les autres champs gardent leur valeur actuelle
     *
     * @param id ID du produit
     * @param dto données de modification (champs optionnels)
     * @return le produit modifié
     * @throws ResourceNotFoundException si produit non trouvé
     * @throws IllegalArgumentException si categoryId invalide
     */
    ProductResponseDto update(Long id, ProductUpdateDto dto);

    /**
     * Récupère un produit par ID
     *
     * @param id ID du produit
     * @return le produit
     * @throws ResourceNotFoundException si non trouvé
     */
    ProductResponseDto findById(Long id);

    /**
     * Liste tous les produits avec pagination
     *
     * CONCEPT: Pagination
     * - Évite de charger toute la table en mémoire
     * - Performance: limite les données transférées
     * - UX: affichage par page
     *
     * @param pageable configuration pagination (page, size, sort)
     * @return page de produits
     */
    List<ProductResponseDto> findAll();

    /**
     * Recherche les produits par catégorie
     *
     * @param categoryId ID de la catégorie
     * @param pageable pagination
     * @return page de produits de cette catégorie
     */
    Page<ProductResponseDto> findByCategory(Long categoryId, Pageable pageable);


    ProductResponseDto changeQuantity(Long id, int newQuantity);

    void delete(Long id);
    Page<ProductResponseDto> searchProducts(ProductFilterDto filter);

    ProductDetailDto getProductDetail(Long productId);

    ProductStatsDto getProductStats(Long productId);

    ProductResponseDto findByReference(String reference);

    Page<ProductResponseDto> searchByName(String name, Pageable pageable);

    List<ProductResponseDto> findProductByName(String name);
    /**
     * Liste les produits actifs uniquement
     *
     * @param pageable pagination
     * @return page de produits actifs
     */
    Page<ProductResponseDto> findAllActive(Pageable pageable);
    ProductResponseDto setActive(Long id, boolean active);


}
