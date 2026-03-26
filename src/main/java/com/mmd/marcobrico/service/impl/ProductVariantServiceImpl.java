package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.ProductVariant;
import com.mmd.marcobrico.dto.product.ProductVariantCreateDto;
import com.mmd.marcobrico.dto.product.ProductVariantResponseDto;
import com.mmd.marcobrico.dto.product.ProductVariantUpdateDto;
import com.mmd.marcobrico.dto.product.StockAdjustmentDto;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.ProductVariantMapper;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.ProductVariantRepository;
import com.mmd.marcobrico.service.ProductVariantService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper variantMapper;

    @Override
    public ProductVariantResponseDto create(Long productId, ProductVariantCreateDto dto) {
        log.info("Création d'une variante pour le produit ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produitt non trouvé avec l'ID: " + productId));
        validateConversionFactor(dto.conversionFactor());

        ProductVariant variant = variantMapper.toEntity(dto, product);
        ProductVariant saveVariant = variantRepository.save(variant);
        return variantMapper.toDto(saveVariant);
    }



    @Override
    @Transactional
    public ProductVariantResponseDto update(Long variantId, ProductVariantUpdateDto dto) {
        log.info("Mise à jour de la variante ID: {}", variantId);

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variante non trouvée avec l'ID: " + variantId));

        // Validation si conversionFactor change
        if (dto.conversionFactor() != null) {
            validateConversionFactor(dto.conversionFactor());
        }

        /**
         * CONCEPT: Modification partielle
         *
         * Seuls les champs non-null sont modifiés
         * Stock n'est PAS modifiable ici (business rule)
         */
        variantMapper.updateEntity(dto, variant);
        // Auto-save via dirty checking

        log.debug("Variante {} mise à jour", variantId);

        return variantMapper.toDto(variant);
    }

    @Override
    public ProductVariantResponseDto findById(Long variantId) {
        log.debug("Recherche variante par ID: {}", variantId);

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variante non trouvée avec l'ID: " + variantId));

        return variantMapper.toDto(variant);
    }

    @Override
    public List<ProductVariantResponseDto> findByProductId(Long productId) {
        log.debug("Liste des variantes du produit ID: {}", productId);

        /**
         * CONCEPT: List vs Page
         *
         * Ici on retourne List car:
         * - Un produit a généralement peu de variants (< 20)
         * - Pas besoin de pagination
         * - Plus simple pour le client
         */
        List<ProductVariant> variants = variantRepository.findByProductId(productId);

        /**
         * CONCEPT: Stream API
         *
         * .stream() = conversion List → Stream
         * .map() = transformation de chaque élément
         * .collect() = reconversion Stream → List
         */
        return variants.stream()
                .map(variantMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductVariantResponseDto> findAll(Pageable pageable) {
        log.debug("Liste de toutes les variantes");

        return variantRepository.findAll(pageable)
                .map(variantMapper::toDto);
    }

    @Override
    public Page<ProductVariantResponseDto> findByWholesale(boolean wholesale, Pageable pageable) {
        log.debug("Liste des variantes wholesale={}", wholesale);

        /**
         * CONCEPT: Query Method avec booléen
         *
         * findByWholesale(true) → WHERE wholesale = true
         * findByWholesale(false) → WHERE wholesale = false
         */
        return variantRepository.findByWholesale(wholesale, pageable)
                .map(variantMapper::toDto);
    }

    @Override
    @Transactional
    public ProductVariantResponseDto addStock(Long variantId, StockAdjustmentDto dto) {
        log.info("Ajout de stock à la variante ID: {} - Quantité: {}",
                variantId, dto.quantity());

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variante non trouvée avec l'ID: " + variantId));


        variant.addStock(dto.quantity());
        // Auto-save via dirty checking

        log.info("Stock ajouté. Nouveau stock: {}", variant.getStock());

        // Option: Log dans une table d'audit
        if (dto.reason() != null) {
            log.info("Raison de l'ajustement: {}", dto.reason());
            // TODO: Sauvegarder dans StockMovement table
        }

        return variantMapper.toDto(variant);
    }

    @Override
    @Transactional
    public ProductVariantResponseDto removeStock(Long variantId, StockAdjustmentDto dto) {
        log.info("Retrait de stock de la variante ID: {} - Quantité: {}",
                variantId, dto.quantity());

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variante non trouvée avec l'ID: " + variantId));

        variant.removeStock(dto.quantity());
        // Auto-save via dirty checking

        log.info("Stock retiré. Nouveau stock: {}", variant.getStock());

        if (dto.reason() != null) {
            log.info("Raison de l'ajustement: {}", dto.reason());
            // TODO: Sauvegarder dans StockMovement table
        }

        return variantMapper.toDto(variant);
    }

    @Override
    @Transactional
    public void delete(Long variantId) {
        log.warn("Suppression définitive de la variante ID: {}", variantId);

        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException(
                    "Variante non trouvée avec l'ID: " + variantId);
        }

        variantRepository.deleteById(variantId);

        log.info("Variante {} supprimée définitivement", variantId);
    }

    @Override
    public boolean hasAvailableStock(Long variantId, BigDecimal quantityNeeded) {
        log.debug("Vérification stock disponible pour variante ID: {}", variantId);

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variante non trouvée avec l'ID: " + variantId));

        boolean hasStock = variant.getStock().compareTo(quantityNeeded) >= 0;

        log.debug("Stock disponible: {} - Quantité demandée: {} - Résultat: {}",
                variant.getStock(), quantityNeeded, hasStock);

        return hasStock;
    }

    @Override
    public Page<ProductVariantResponseDto> findOutOfStock(Pageable pageable) {
        log.debug("Liste des variantes en rupture de stock");

        return variantRepository.findByStock(BigDecimal.ZERO, pageable)
                .map(variantMapper::toDto);
    }

    /**
     * MÉTHODE PRIVÉE: Validation métier
     *
     * CONCEPT: Encapsulation de la validation
     * - Réutilisable dans create() et update()
     * - Centralise les règles métier
     */
    private void validateConversionFactor(BigDecimal conversionFactor) {

        if (conversionFactor == null) {
            throw new IllegalArgumentException(
                    "Le facteur de conversion ne peut pas être null");
        }

        if (conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Le facteur de conversion doit être strictement positif. Valeur fournie: "
                            + conversionFactor);
        }

        if (conversionFactor.compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException(
                    "Le facteur de conversion est trop élevé (max: 1,000,000)");
        }
    }
}
