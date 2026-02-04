package com.mmd.marcobrico.controller;

import com.mmd.marcobrico.dto.product.ProductVariantCreateDto;
import com.mmd.marcobrico.dto.product.ProductVariantResponseDto;
import com.mmd.marcobrico.dto.product.ProductVariantUpdateDto;
import com.mmd.marcobrico.dto.product.StockAdjustmentDto;
import com.mmd.marcobrico.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

    private final ProductVariantService variantService;

        /* ======================================================
       ENDPOINTS NESTED: /api/products/{productId}/variants
       ====================================================== */


    /**
     * ENDPOINT: Créer une variante pour un produit
     *
     * CONCEPT: Nested Resource Creation
     *
     * HTTP: POST /api/products/5/variants
     * Body: { "saleUnit": "PIECE", "price": 15.99, ... }
     * Response: 201 CREATED + variante créée
     *
     * Pattern REST: ressource enfant créée via parent
     */
    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ProductVariantResponseDto> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantCreateDto dto) {

        log.info("POST /api/products/{}/variants", productId);

        ProductVariantResponseDto created = variantService.create(productId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<ProductVariantResponseDto>> getVariantsByProduct(
            @PathVariable Long productId) {

        log.debug("GET /api/products/{}/variants", productId);

        List<ProductVariantResponseDto> variants =
                variantService.findByProductId(productId);

        return ResponseEntity.ok(variants);
    }

    /**
     * ENDPOINT: Récupérer une variante par ID
     *
     * CONCEPT: Direct Resource Access
     *
     * HTTP: GET /api/variants/123
     * Response: 200 OK + variante
     *           404 si inexistante
     *
     * Accès direct sans passer par le product parent
     */
    @GetMapping("/variants/{id}")
    public ResponseEntity<ProductVariantResponseDto> getVariantById(
            @PathVariable Long id) {

        log.debug("GET /api/variants/{}", id);

        ProductVariantResponseDto variant = variantService.findById(id);

        return ResponseEntity.ok(variant);
    }

    /**
     * ENDPOINT: Lister toutes les variantes (paginé)
     *
     * CONCEPT: Global Collection
     *
     * HTTP: GET /api/variants?page=0&size=20
     * Response: 200 OK + page de variantes
     *
     * Utile pour administration, inventaire global
     */
    @GetMapping("/variants")
    public ResponseEntity<Page<ProductVariantResponseDto>> getAllVariants(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        log.debug("GET /api/variants");

        Page<ProductVariantResponseDto> variants = variantService.findAll(pageable);

        return ResponseEntity.ok(variants);
    }
    /**
     * ENDPOINT: Filtrer par type de vente
     *
     * CONCEPT: Query Parameter Filtering
     *
     * HTTP: GET /api/variants?wholesale=true
     * Response: 200 OK + page de variantes wholesale
     *
     * @RequestParam(required = false) = optionnel
     */
    @GetMapping(value = "/variants", params = "wholesale")
    public ResponseEntity<Page<ProductVariantResponseDto>> getVariantsByWholesale(
            @RequestParam boolean wholesale,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("GET /api/variants?wholesale={}", wholesale);

        Page<ProductVariantResponseDto> variants =
                variantService.findByWholesale(wholesale, pageable);

        return ResponseEntity.ok(variants);
    }

    /**
     * ENDPOINT: Mettre à jour une variante
     *
     * CONCEPT: Partial Update
     *
     * HTTP: PATCH /api/variants/123
     * Body: { "price": 19.99 }
     * Response: 200 OK + variante modifiée
     *
     * Stock n'est PAS modifiable ici
     * Utilisez /stock/add ou /stock/remove
     */
    @PatchMapping("/variants/{id}")
    public ResponseEntity<ProductVariantResponseDto> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantUpdateDto dto) {

        log.info("PATCH /api/variants/{}", id);

        ProductVariantResponseDto updated = variantService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    /* ======================================================
       ENDPOINTS STOCK MANAGEMENT
       ====================================================== */

    /**
     * ENDPOINT: Ajouter du stock
     *
     * CONCEPT: Domain Action Endpoint
     *
     * HTTP: POST /api/variants/123/stock/add
     * Body: { "quantity": 50, "reason": "Réapprovisionnement" }
     * Response: 200 OK + variante avec nouveau stock
     *
     * Pattern: Action as POST
     * - POST car on "crée" un mouvement de stock
     * - Même si techniquement c'est un UPDATE
     */
    @PostMapping("/variants/{id}/stock/add")
    public ResponseEntity<ProductVariantResponseDto> addStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentDto dto) {

        log.info("POST /api/variants/{}/stock/add - Quantity: {}",
                id, dto.quantity());

        /**
         * CONCEPT: Business Operation
         *
         * addStock() n'est pas juste un setter
         * C'est une opération métier qui:
         * - Valide la quantité
         * - Met à jour le stock
         * - Peut créer un historique
         * - Peut déclencher des événements
         */
        ProductVariantResponseDto updated = variantService.addStock(id, dto);

        return ResponseEntity.ok(updated);
    }

    /**
     * ENDPOINT: Retirer du stock
     *
     * CONCEPT: Business Validation
     *
     * HTTP: POST /api/variants/123/stock/remove
     * Body: { "quantity": 10, "reason": "Vente" }
     * Response: 200 OK + variante avec nouveau stock
     *           409 CONFLICT si stock insuffisant
     *
     * Lève IllegalStateException si stock < quantité
     * → GlobalExceptionHandler → 409 CONFLICT
     */
    @PostMapping("/variants/{id}/stock/remove")
    public ResponseEntity<ProductVariantResponseDto> removeStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentDto dto) {

        log.info("POST /api/variants/{}/stock/remove - Quantity: {}",
                id, dto.quantity());

        ProductVariantResponseDto updated = variantService.removeStock(id, dto);

        return ResponseEntity.ok(updated);
    }

    /**
     * ENDPOINT: Vérifier disponibilité du stock
     *
     * CONCEPT: Query Endpoint
     *
     * HTTP: GET /api/variants/123/stock/check?quantity=25
     * Response: 200 OK + { "available": true }
     *
     * Utile avant une commande pour vérifier disponibilité
     */
    @GetMapping("/variants/{id}/stock/check")
    public ResponseEntity<StockAvailabilityResponse> checkStockAvailability(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity) {

        log.debug("GET /api/variants/{}/stock/check?quantity={}", id, quantity);

        boolean available = variantService.hasAvailableStock(id, quantity);

        /**
         * CONCEPT: DTO Response pour boolean
         *
         * Au lieu de retourner juste true/false,
         * on retourne un objet structuré:
         * { "available": true, "variantId": 123 }
         *
         * Plus extensible (on peut ajouter stockLeft, etc.)
         */
        StockAvailabilityResponse response =
                new StockAvailabilityResponse(id, quantity, available);

        return ResponseEntity.ok(response);
    }

    /**
     * ENDPOINT: Lister les variantes en rupture
     *
     * CONCEPT: Filtered Collection
     *
     * HTTP: GET /api/variants/out-of-stock
     * Response: 200 OK + page de variantes (stock = 0)
     *
     * Endpoint métier spécifique pour gestion inventaire
     */
    @GetMapping("/variants/out-of-stock")
    public ResponseEntity<Page<ProductVariantResponseDto>> getOutOfStockVariants(
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("GET /api/variants/out-of-stock");

        Page<ProductVariantResponseDto> variants =
                variantService.findOutOfStock(pageable);

        return ResponseEntity.ok(variants);
    }

    /**
     * ENDPOINT: Supprimer une variante
     *
     * CONCEPT: Hard Delete
     *
     * HTTP: DELETE /api/variants/123
     * Response: 204 NO CONTENT
     *           404 si inexistante
     *           409 si contraintes FK
     */
    @DeleteMapping("/variants/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {

        log.warn("DELETE /api/variants/{}", id);

        variantService.delete(id);

        return ResponseEntity.noContent().build();
    }

    /* ======================================================
       DTOs INTERNES AU CONTROLLER
       ====================================================== */

    /**
     * CONCEPT: Inner Record DTO
     *
     * DTO simple utilisé uniquement par ce controller
     * Pas besoin d'un fichier séparé
     */
    public record StockAvailabilityResponse(
            Long variantId,
            BigDecimal quantityRequested,
            boolean available
    ) {}

}
