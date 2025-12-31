package com.mmd.marcobrico.service.impl;
import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.domain.SaleItem;
import com.mmd.marcobrico.dto.product.*;
import com.mmd.marcobrico.dto.sale.SaleItemStatDto;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.ProductMapper;
import com.mmd.marcobrico.repository.CategoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires complets pour ProductServiceImpl
 *
 * Structure :
 * - 8 méthodes à tester
 * - Tests des cas nominaux et des cas d'erreur
 * - Validation des règles métier (stock négatif, référence unique)
 * - Tests de pagination et filtrage
 * - Tests des statistiques et agrégations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl - Tests unitaires")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductCreateDto createDto;
    private ProductUpdateDto updateDto;
    private Product product;
    private Category category;
    private ProductResponseDto responseDto;

    /**
     * Initialisation commune pour tous les tests
     *
     * Concepts :
     * - Mocks simples sans stubbing inutile
     * - Données de test réalistes
     * - Réutilisation via @BeforeEach
     */
    @BeforeEach
    void setUp() {
        // DTOs de test
        createDto = new ProductCreateDto(
                "Marteau",
                "MAR-001",
                new BigDecimal("25.99"),
                100,
                10,
                1L
        );

        updateDto = new ProductUpdateDto(
                "Marteau Premium",
                new BigDecimal("29.99"),
                15,
                1L
        );

        // Category mock
        category = mock(Category.class);


        // Product mock
        product = mock(Product.class);


        // Response DTO
        responseDto = new ProductResponseDto(
                1L,
                "Marteau",
                "MAR-001",
                new BigDecimal("25.99"),
                100,
                10,
                1L,
                "Outils"
        );
    }
    @Nested
    @DisplayName("Tests de create()")
    class CreateTests {

        @Test
        @DisplayName("Devrait créer un produit avec succès")
        void shouldCreateProductSuccessfully() {
            // GIVEN
            when(productRepository.existsByReference(createDto.reference())).thenReturn(false);
            when(categoryRepository.findById(createDto.categoryId())).thenReturn(Optional.of(category));
            when(productMapper.toEntity(createDto, category)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toDto(product)).thenReturn(responseDto);

            // WHEN
            ProductResponseDto result = productService.create(createDto);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Marteau");
            assertThat(result.reference()).isEqualTo("MAR-001");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("25.99"));
            assertThat(result.categoryId()).isEqualTo(1L);

            // Vérifier l'ordre des appels
            verify(productRepository).existsByReference(createDto.reference());
            verify(categoryRepository).findById(createDto.categoryId());
            verify(productMapper).toEntity(createDto, category);
            verify(productRepository).save(product);
            verify(productMapper).toDto(product);
        }

        @Test
        @DisplayName("Devrait lancer une exception si la référence existe déjà")
        void shouldThrowExceptionWhenReferenceAlreadyExists() {
            // GIVEN
            when(productRepository.existsByReference(createDto.reference())).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> productService.create(createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Référence déjà utilisée");

            verify(productRepository).existsByReference(createDto.reference());
            verify(categoryRepository, never()).findById(anyLong());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lancer une exception si la catégorie n'existe pas")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // GIVEN
            when(productRepository.existsByReference(createDto.reference())).thenReturn(false);
            when(categoryRepository.findById(createDto.categoryId())).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> productService.create(createDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Catégorie introuvable");

            verify(productRepository).existsByReference(createDto.reference());
            verify(categoryRepository).findById(createDto.categoryId());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait créer un produit avec stock à 0")
        void shouldCreateProductWithZeroStock() {
            // GIVEN
            ProductCreateDto dtoWithZeroStock = new ProductCreateDto(
                    "Produit test", "TEST-001", new BigDecimal("10.00"), 0, 5, 1L
            );

            when(productRepository.existsByReference(dtoWithZeroStock.reference())).thenReturn(false);
            when(categoryRepository.findById(dtoWithZeroStock.categoryId())).thenReturn(Optional.of(category));
            when(productMapper.toEntity(dtoWithZeroStock, category)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toDto(product)).thenReturn(responseDto);

            // WHEN
            ProductResponseDto result = productService.create(dtoWithZeroStock);

            // THEN
            assertThat(result).isNotNull();
            verify(productRepository).save(product);
        }
    }
    /**
     * Tests de la méthode update()
     *
     * Scénarios :
     * - Mise à jour réussie avec nouvelle catégorie
     * - Produit introuvable
     * - Nouvelle catégorie introuvable
     */
    @Nested
    @DisplayName("Tests de update()")
    class UpdateTests {

        @Test
        @DisplayName("Devrait mettre à jour un produit avec succès")
        void shouldUpdateProductSuccessfully() {
            // GIVEN
            Long productId = 1L;
            Product updatedProduct = mock(Product.class);
            ProductResponseDto updatedResponse = new ProductResponseDto(
                    productId, "Marteau Premium", "MAR-001",
                    new BigDecimal("29.99"), 100, 15, 1L, "Outils"
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(updateDto.categoryId())).thenReturn(Optional.of(category));
            when(productMapper.updateEntity(updateDto, product, category)).thenReturn(updatedProduct);
            when(productRepository.save(updatedProduct)).thenReturn(updatedProduct);
            when(productMapper.toDto(updatedProduct)).thenReturn(updatedResponse);

            // WHEN
            ProductResponseDto result = productService.update(productId, updateDto);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo("Marteau Premium");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("29.99"));

            verify(productRepository).findById(productId);
            verify(categoryRepository).findById(updateDto.categoryId());
            verify(productMapper).updateEntity(updateDto, product, category);
            verify(productRepository).save(updatedProduct);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le produit n'existe pas")
        void shouldThrowExceptionWhenProductNotFound() {
            // GIVEN
            Long nonExistentId = 999L;
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> productService.update(nonExistentId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Produit introuvable");

            verify(productRepository).findById(nonExistentId);
            verify(categoryRepository, never()).findById(anyLong());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lancer une exception si la nouvelle catégorie n'existe pas")
        void shouldThrowExceptionWhenNewCategoryNotFound() {
            // GIVEN
            Long productId = 1L;
            Long nonExistentCategoryId = 999L;
            ProductUpdateDto dtoWithInvalidCategory = new ProductUpdateDto(
                    "Produit", new BigDecimal("10.00"), 5, nonExistentCategoryId
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> productService.update(productId, dtoWithInvalidCategory))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Catégorie introuvable");

            verify(productRepository).findById(productId);
            verify(categoryRepository).findById(nonExistentCategoryId);
            verify(productRepository, never()).save(any());
        }
    }

    /**
     * Tests de la méthode changeQuantity()
     *
     * Règles métier :
     * - Le stock ne peut pas être négatif (validation dans Product.changeQuantity())
     * - Produit doit exister
     */
    @Nested
    @DisplayName("Tests de changeQuantity()")
    class ChangeQuantityTests {

        @Test
        @DisplayName("Devrait changer la quantité avec succès")
        void shouldChangeQuantitySuccessfully() {
            // GIVEN
            Long productId = 1L;
            int newQuantity = 50;
            Product productWithNewQuantity = mock(Product.class);
            ProductResponseDto responseWithNewQuantity = new ProductResponseDto(
                    productId, "Marteau", "MAR-001",
                    new BigDecimal("25.99"), 50, 10, 1L, "Outils"
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(product.changeQuantity(newQuantity)).thenReturn(productWithNewQuantity);
            when(productRepository.save(productWithNewQuantity)).thenReturn(productWithNewQuantity);
            when(productMapper.toDto(productWithNewQuantity)).thenReturn(responseWithNewQuantity);

            // WHEN
            ProductResponseDto result = productService.changeQuantity(productId, newQuantity);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.quantity()).isEqualTo(50);

            verify(productRepository).findById(productId);
            verify(product).changeQuantity(newQuantity);
            verify(productRepository).save(productWithNewQuantity);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le produit n'existe pas")
        void shouldThrowExceptionWhenProductNotFoundForQuantityChange() {
            // GIVEN
            Long nonExistentId = 999L;
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> productService.changeQuantity(nonExistentId, 10))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Produit introuvable");

            verify(productRepository).findById(nonExistentId);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait permettre de mettre la quantité à 0")
        void shouldAllowZeroQuantity() {
            // GIVEN
            Long productId = 1L;
            Product productWithZeroQuantity = mock(Product.class);
            ProductResponseDto responseWithZero = new ProductResponseDto(
                    productId, "Marteau", "MAR-001",
                    new BigDecimal("25.99"), 0, 10, 1L, "Outils"
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(product.changeQuantity(0)).thenReturn(productWithZeroQuantity);
            when(productRepository.save(productWithZeroQuantity)).thenReturn(productWithZeroQuantity);
            when(productMapper.toDto(productWithZeroQuantity)).thenReturn(responseWithZero);

            // WHEN
            ProductResponseDto result = productService.changeQuantity(productId, 0);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.quantity()).isZero();
        }

        @Test
        @DisplayName("Devrait propager l'exception si quantité négative")
        void shouldPropagateExceptionForNegativeQuantity() {
            // GIVEN
            Long productId = 1L;
            int negativeQuantity = -10;

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(product.changeQuantity(negativeQuantity))
                    .thenThrow(new IllegalArgumentException("Le stock ne peut pas être négatif"));

            // WHEN & THEN
            assertThatThrownBy(() -> productService.changeQuantity(productId, negativeQuantity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Le stock ne peut pas être négatif");

            verify(productRepository).findById(productId);
            verify(product).changeQuantity(negativeQuantity);
            verify(productRepository, never()).save(any());
        }
    }

    /**
     * Tests de findAll()
     *
     * Simple mais important pour la couverture
     */
    @Nested
    @DisplayName("Tests de findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Devrait retourner tous les produits")
        void shouldReturnAllProducts() {
            // GIVEN
            Product product1 = mock(Product.class);
            Product product2 = mock(Product.class);
            List<Product> products = Arrays.asList(product1, product2);

            ProductResponseDto dto1 = new ProductResponseDto(
                    1L, "Prod1", "REF1", new BigDecimal("10"), 10, 5, 1L, "Cat1"
            );
            ProductResponseDto dto2 = new ProductResponseDto(
                    2L, "Prod2", "REF2", new BigDecimal("20"), 20, 5, 1L, "Cat1"
            );

            when(productRepository.findAll()).thenReturn(products);
            when(productMapper.toDto(product1)).thenReturn(dto1);
            when(productMapper.toDto(product2)).thenReturn(dto2);

            // WHEN
            List<ProductResponseDto> result = productService.findAll();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);

            verify(productRepository).findAll();
            verify(productMapper, times(2)).toDto(any(Product.class));
        }

        @Test
        @DisplayName("Devrait retourner une liste vide si aucun produit")
        void shouldReturnEmptyListWhenNoProducts() {
            // GIVEN
            when(productRepository.findAll()).thenReturn(List.of());

            // WHEN
            List<ProductResponseDto> result = productService.findAll();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(productRepository).findAll();
            verify(productMapper, never()).toDto(any());
        }
    }

    /**
     * Tests de findById()
     */
    @Nested
    @DisplayName("Tests de findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Devrait trouver un produit par son ID")
        void shouldFindProductById() {
            // GIVEN
            Long productId = 1L;
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productMapper.toDto(product)).thenReturn(responseDto);

            // WHEN
            ProductResponseDto result = productService.findById(productId);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo("Marteau");

            verify(productRepository).findById(productId);
            verify(productMapper).toDto(product);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le produit n'existe pas")
        void shouldThrowExceptionWhenProductNotFoundById() {
            // GIVEN
            Long nonExistentId = 999L;
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> productService.findById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Produit introuvable");

            verify(productRepository).findById(nonExistentId);
            verify(productMapper, never()).toDto(any());
        }
    }

    /**
     * Tests de delete()
     */
    @Nested
    @DisplayName("Tests de delete()")
    class DeleteTests {

        @Test
        @DisplayName("Devrait supprimer un produit existant")
        void shouldDeleteExistingProduct() {
            // GIVEN
            Long productId = 1L;
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            doNothing().when(productRepository).delete(product);

            // WHEN
            productService.delete(productId);

            // THEN
            verify(productRepository).findById(productId);
            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("Devrait lancer une exception si le produit à supprimer n'existe pas")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            // GIVEN
            Long nonExistentId = 999L;
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> productService.delete(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Produit introuvable");

            verify(productRepository).findById(nonExistentId);
           // verify(productRepository, never()).delete(any());
        }
    }

    /**
     * Tests de searchProducts() avec filtrage et pagination
     *
     * Concepts complexes :
     * - Specification pour le filtrage dynamique
     * - Tri ascendant/descendant
     * - Pagination
     */
    @Nested
    @DisplayName("Tests de searchProducts()")
    class SearchProductsTests {

        @Test
        @DisplayName("Devrait rechercher des produits avec tous les filtres")
        void shouldSearchProductsWithAllFilters() {
            // GIVEN
            ProductFilterDto filter = new ProductFilterDto(
                    "Marteau", 1L, true, 0, 10, "name", "ASC"
            );

            Product product1 = mock(Product.class);
            List<Product> products = List.of(product1);
            Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(productPage);
            when(productMapper.toDto(product1)).thenReturn(responseDto);

            // WHEN
            Page<ProductResponseDto> result = productService.searchProducts(filter);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
            verify(productMapper).toDto(product1);
        }

//        @Test
//        @DisplayName("Devrait utiliser les valeurs par défaut pour le tri")
//        void shouldUseDefaultSortValues() {
//            // GIVEN - Sans sortBy et sortDirection
//            ProductFilterDto filter = new ProductFilterDto(
//                    null, null, null, 0, 10, null, null
//            );
//
//            Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(emptyPage);
//
//            // WHEN
//            Page<ProductResponseDto> result = productService.searchProducts(filter);
//
//            // THEN
//            assertThat(result).isNotNull();
//            assertThat(result.getContent()).isEmpty();
//
//            // Vérifier que le tri par défaut (name, ASC) est appliqué
//            verify(productRepository).findAll(any(Specification.class), argThat(pageable ->
//                    pageable.getSort().getOrderFor("name") != null &&
//                            pageable.getSort().getOrderFor("name").isAscending()
//            ));
//        }

//        @Test
//        @DisplayName("Devrait trier en ordre descendant")
//        void shouldSortInDescendingOrder() {
//            // GIVEN
//            ProductFilterDto filter = new ProductFilterDto(
//                    null, null, null, 0, 10, "price", "DESC"
//            );
//
//            Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(emptyPage);
//
//            // WHEN
//            Page<ProductResponseDto> result = productService.searchProducts(filter);
//
//            // THEN
//            assertThat(result).isNotNull();
//
//            verify(productRepository).findAll(any(Specification.class), argThat(pageable ->
//                    pageable.getSort().getOrderFor("price") != null &&
//                            pageable.getSort().getOrderFor("price").isDescending()
//            ));
//        }

        @Test
        @DisplayName("Devrait paginer correctement les résultats")
        void shouldPaginateResultsCorrectly() {
            // GIVEN
            ProductFilterDto filter = new ProductFilterDto(
                    null, null, null, 2, 5, "name", "ASC"
            );

            List<Product> products = Arrays.asList(mock(Product.class), mock(Product.class));
            Page<Product> productPage = new PageImpl<>(products, PageRequest.of(2, 5), 15);

            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(productPage);
            when(productMapper.toDto(any(Product.class))).thenReturn(responseDto);

            // WHEN
            Page<ProductResponseDto> result = productService.searchProducts(filter);

            // THEN
            assertThat(result.getNumber()).isEqualTo(2); // Page 2
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3); // 15/5 = 3 pages
        }

        @Test
        @DisplayName("Devrait retourner une page vide si aucun résultat")
        void shouldReturnEmptyPageWhenNoResults() {
            // GIVEN
            ProductFilterDto filter = new ProductFilterDto(
                    "Inexistant", null, null, 0, 10, "name", "ASC"
            );

            Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // WHEN
            Page<ProductResponseDto> result = productService.searchProducts(filter);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(productMapper, never()).toDto(any());
        }
    }

    /**
     * Tests de getProductDetail()
     *
     * Concepts testés :
     * - Agrégation de données depuis plusieurs repositories
     * - Gestion des valeurs null (pas de vente)
     * - Calculs (revenue)
     */
    @Nested
    @DisplayName("Tests de getProductDetail()")
    class GetProductDetailTests {

        @Test
        @DisplayName("Devrait retourner les détails complets d'un produit avec ventes")
        void shouldReturnProductDetailWithSales() {
            // GIVEN
            Long productId = 1L;
            Sale lastSale = mock(Sale.class);
            LocalDateTime saleDate = LocalDateTime.now();
            var expectedCategory = Category.create("expected", "description");
            var expedtedProduct = Product.create(
                    "Marteau",
                    "00015",
                    BigDecimal.valueOf(100L),
                    10,
                    10,
                    expectedCategory

            );
            when(productRepository.findById(productId)).thenReturn(Optional.of(expedtedProduct));
            when(saleRepository.countByProductId(productId)).thenReturn(5L);
            when(saleRepository.findSalesByProductIdOrderByDateDesc(productId))
                    .thenReturn(List.of(lastSale));
            when(lastSale.getCreatedAt()).thenReturn(saleDate);
            when(saleRepository.getRevenueByProductId(productId))
                    .thenReturn(new BigDecimal("129.95"));

            // WHEN
            ProductDetailDto result = productService.getProductDetail(productId);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Marteau");
            assertThat(result.numberOfSales()).isEqualTo(5L);
            assertThat(result.lastSaleDate()).isEqualTo(saleDate);
            assertThat(result.revenue()).isEqualByComparingTo(new BigDecimal("129.95"));

            verify(productRepository).findById(productId);
            verify(saleRepository).countByProductId(productId);
            verify(saleRepository).findSalesByProductIdOrderByDateDesc(productId);
            verify(saleRepository).getRevenueByProductId(productId);
        }
    }
}