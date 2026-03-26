package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.CategoryMapper;
import com.mmd.marcobrico.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


/**
 * Tests unitaires pour CategoryServiceImpl
 *
 * Architecture des tests :
 * - @ExtendWith(MockitoExtension.class) : Active Mockito pour les tests
 * - @Mock : Crée des mocks pour les dépendances
 * - @InjectMocks : Injecte automatiquement les mocks dans le service
 * - @Nested : Organise les tests par fonctionnalité
 * - @DisplayName : Descriptions lisibles des tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl - Tests unitaires")
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository repository;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    private CategoryCreateDto createDto;
    private CategoryUpdateDto updateDto;
    private Category category;
    private CategoryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        createDto = new CategoryCreateDto("Électronique", "Produits électroniques");
        updateDto = new CategoryUpdateDto("Électronique Mise à jour", "Description mise à jour");

        category = mock(Category.class);


        responseDto = new CategoryResponseDto(
                1L,
                "Électronique",
                "Produits électroniques",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
    /**
     * Tests pour la méthode create()
     *
     * Scénarios testés :
     * 1. Création réussie avec toutes les données
     * 2. Création avec description null
     * 3. Exception si catégorie déjà existante
     */
    @Nested
    @DisplayName("Tests de la méthode create()")
    class CreateTests {

        @Test
        @DisplayName("Devrait créer une catégorie avec succès")
        void shouldCreateCategorySuccessfully() {
            // GIVEN
            when(repository.existsByNameIgnoreCase(createDto.name())).thenReturn(false);
            when(mapper.createEntity(createDto)).thenReturn(category);
            when(repository.save(category)).thenReturn(category);
            when(mapper.toDto(category)).thenReturn(responseDto);

            // WHEN
            CategoryResponseDto result = categoryService.create(createDto);

            // THEN

            assertThat(result).isNotNull();

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Électronique");
            assertThat(result.description()).isEqualTo("Produits électroniques");


            verify(repository).existsByNameIgnoreCase(createDto.name());
            verify(mapper).createEntity(createDto);
            verify(repository).save(category);
            verify(mapper).toDto(category);


            verifyNoMoreInteractions(repository, mapper);
        }

        @Test
        @DisplayName("Devrait créer une catégorie avec description null")
        void shouldCreateCategoryWithNullDescription() {
            // GIVEN
            CategoryCreateDto dtoWithNullDesc = new CategoryCreateDto("Sport", null);
            Category categoryWithNullDesc = mock(Category.class);
            CategoryResponseDto responseWithNullDesc = new CategoryResponseDto(
                    2L, "Sport", null, LocalDateTime.now(), LocalDateTime.now()
            );

            when(repository.existsByNameIgnoreCase(dtoWithNullDesc.name())).thenReturn(false);
            when(mapper.createEntity(dtoWithNullDesc)).thenReturn(categoryWithNullDesc);
            when(repository.save(categoryWithNullDesc)).thenReturn(categoryWithNullDesc);
            when(mapper.toDto(categoryWithNullDesc)).thenReturn(responseWithNullDesc);

            // WHEN
            CategoryResponseDto result = categoryService.create(dtoWithNullDesc);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.description()).isNull();
            verify(repository).existsByNameIgnoreCase(dtoWithNullDesc.name());
            verify(repository).save(categoryWithNullDesc);
        }

        @Test
        @DisplayName("Devrait lancer IllegalArgumentException si catégorie existe déjà")
        void shouldThrowExceptionWhenCategoryAlreadyExists() {
            // GIVEN
            when(repository.existsByNameIgnoreCase(createDto.name())).thenReturn(true);

            // WHEN & THEN

            assertThatThrownBy(() -> categoryService.create(createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Catégorie déjà existante");


            verify(repository).existsByNameIgnoreCase(createDto.name());
            verify(repository, never()).save(any());
            verify(mapper, never()).createEntity(any());
        }

        @Test
        @DisplayName("Devrait gérer les noms avec différentes casses")
        void shouldHandleCaseInsensitiveNameCheck() {
            // GIVEN
            CategoryCreateDto dtoUpperCase = new CategoryCreateDto("ÉLECTRONIQUE", "Description");
            when(repository.existsByNameIgnoreCase("ÉLECTRONIQUE")).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> categoryService.create(dtoUpperCase))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(repository).existsByNameIgnoreCase("ÉLECTRONIQUE");
        }
    }
    /**
     * Tests pour la méthode update()
     *
     * Concepts testés :
     * - Mise à jour réussie
     * - Exception ResourceNotFoundException
     * - Utilisation de l'entité existante
     */
    @Nested
    @DisplayName("Tests de la méthode update()")
    class UpdateTests {

        @Test
        @DisplayName("Devrait mettre à jour une catégorie existante")
        void shouldUpdateExistingCategory() {
            // GIVEN
            Long categoryId = 1L;
            Category updatedCategory = mock(Category.class);
            CategoryResponseDto updatedResponse = new CategoryResponseDto(
                    categoryId,
                    "Électronique Mise à jour",
                    "Description mise à jour",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(repository.findById(categoryId)).thenReturn(Optional.of(category));
            when(mapper.updateEntity(updateDto, category)).thenReturn(updatedCategory);
            when(repository.save(updatedCategory)).thenReturn(updatedCategory);
            when(mapper.toDto(updatedCategory)).thenReturn(updatedResponse);

            // WHEN
            CategoryResponseDto result = categoryService.update(categoryId, updateDto);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(categoryId);
            assertThat(result.name()).isEqualTo("Électronique Mise à jour");
            assertThat(result.description()).isEqualTo("Description mise à jour");


            verify(repository).findById(categoryId);
            verify(mapper).updateEntity(updateDto, category);
            verify(repository).save(updatedCategory);
            verify(mapper).toDto(updatedCategory);
        }

        @Test
        @DisplayName("Devrait lancer ResourceNotFoundException si catégorie introuvable")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // GIVEN
            Long nonExistentId = 999L;
            when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> categoryService.update(nonExistentId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Catégorie introuvable");

            verify(repository).findById(nonExistentId);
            verify(mapper, never()).updateEntity(any(), any());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait mettre à jour avec description null")
        void shouldUpdateWithNullDescription() {
            // GIVEN
            Long categoryId = 1L;
            CategoryUpdateDto dtoWithNullDesc = new CategoryUpdateDto("Nouveau nom", null);
            Category updatedCategory = mock(Category.class);
            CategoryResponseDto updatedResponse = new CategoryResponseDto(
                    categoryId, "Nouveau nom", null, LocalDateTime.now(), LocalDateTime.now()
            );

            when(repository.findById(categoryId)).thenReturn(Optional.of(category));
            when(mapper.updateEntity(dtoWithNullDesc, category)).thenReturn(updatedCategory);
            when(repository.save(updatedCategory)).thenReturn(updatedCategory);
            when(mapper.toDto(updatedCategory)).thenReturn(updatedResponse);

            // WHEN
            CategoryResponseDto result = categoryService.update(categoryId, dtoWithNullDesc);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.description()).isNull();
        }
    }

    /**
     * Tests pour la méthode getCategories()
     *
     * Concepts de pagination testés :
     * - Page avec plusieurs éléments
     * - Page vide
     * - Différentes tailles de page
     */
    @Nested
    @DisplayName("Tests de la méthode getCategories()")
    class GetCategoriesTests {

        @Test
        @DisplayName("Devrait retourner une page de catégories")
        void shouldReturnPageOfCategories() {
            // GIVEN
            Pageable pageable = PageRequest.of(0, 10);

            Category category1 = mock(Category.class);
            Category category2 = mock(Category.class);
            List<Category> categories = Arrays.asList(category1, category2);
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 2);

            CategoryResponseDto dto1 = new CategoryResponseDto(
                    1L, "Cat1", "Desc1", LocalDateTime.now(), LocalDateTime.now()
            );
            CategoryResponseDto dto2 = new CategoryResponseDto(
                    2L, "Cat2", "Desc2", LocalDateTime.now(), LocalDateTime.now()
            );

            when(repository.findAll(pageable)).thenReturn(categoryPage);
            when(mapper.toDto(category1)).thenReturn(dto1);
            when(mapper.toDto(category2)).thenReturn(dto2);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.getCategories(pageable);

            // THEN

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);


            assertThat(result.getContent().get(0).id()).isEqualTo(1L);
            assertThat(result.getContent().get(1).id()).isEqualTo(2L);

            verify(repository).findAll(pageable);
            verify(mapper, times(2)).toDto(any(Category.class));
        }

        @Test
        @DisplayName("Devrait retourner une page vide si aucune catégorie")
        void shouldReturnEmptyPageWhenNoCategories() {
            // GIVEN
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findAll(pageable)).thenReturn(emptyPage);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.getCategories(pageable);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();

            verify(repository).findAll(pageable);
            verify(mapper, never()).toDto(any());
        }

        @Test
        @DisplayName("Devrait gérer différentes tailles de page")
        void shouldHandleDifferentPageSizes() {
            // GIVEN
            Pageable pageable = PageRequest.of(1, 5);
            List<Category> categories = Arrays.asList(
                    mock(Category.class), mock(Category.class)
            );
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 12);

            when(repository.findAll(pageable)).thenReturn(categoryPage);
            when(mapper.toDto(any(Category.class))).thenReturn(responseDto);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.getCategories(pageable);

            // THEN
            assertThat(result.getNumber()).isEqualTo(1); // Page 1 (deuxième page)
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(12);
            assertThat(result.getTotalPages()).isEqualTo(3); // 12 éléments / 5 par page = 3 pages
        }
    }

    /**
     * Tests pour la méthode search()
     *
     * Concepts testés :
     * - Recherche par nom
     * - Recherche par description
     * - Recherche insensible à la casse
     * - Pagination des résultats
     */
    @Nested
    @DisplayName("Tests de la méthode search()")
    class SearchTests {

        @Test
        @DisplayName("Devrait rechercher des catégories par mot-clé")
        void shouldSearchCategoriesByKeyword() {
            // GIVEN
            String keyword = "électro";
            Pageable pageable = PageRequest.of(0, 10);

            Category category1 = mock(Category.class);
            List<Category> categories = List.of(category1);
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);

            when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword, pageable
            )).thenReturn(categoryPage);
            when(mapper.toResponseDto(category1)).thenReturn(responseDto);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.search(keyword, pageable);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);


            verify(repository).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    eq(keyword), eq(keyword), eq(pageable)
            );
            verify(mapper).toResponseDto(category1);
        }

        @Test
        @DisplayName("Devrait retourner une page vide si aucun résultat")
        void shouldReturnEmptyPageWhenNoSearchResults() {
            // GIVEN
            String keyword = "inexistant";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword, pageable
            )).thenReturn(emptyPage);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.search(keyword, pageable);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(mapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("Devrait rechercher avec des caractères spéciaux")
        void shouldSearchWithSpecialCharacters() {
            // GIVEN
            String keyword = "électro-ménager";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword, pageable
            )).thenReturn(emptyPage);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.search(keyword, pageable);

            // THEN
            assertThat(result).isNotNull();
            verify(repository).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    eq(keyword), eq(keyword), eq(pageable)
            );
        }

        @Test
        @DisplayName("Devrait rechercher avec un mot-clé vide")
        void shouldSearchWithEmptyKeyword() {
            // GIVEN
            String emptyKeyword = "";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    emptyKeyword, emptyKeyword, pageable
            )).thenReturn(categoryPage);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.search(emptyKeyword, pageable);

            // THEN
            assertThat(result).isNotNull();
            verify(repository).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    eq(emptyKeyword), eq(emptyKeyword), eq(pageable)
            );
        }

        @Test
        @DisplayName("Devrait paginer correctement les résultats de recherche")
        void shouldPaginateSearchResultsCorrectly() {
            // GIVEN
            String keyword = "tech";
            Pageable pageable = PageRequest.of(1, 5);

            List<Category> categories = Arrays.asList(
                    mock(Category.class), mock(Category.class), mock(Category.class)
            );
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 15);

            when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword, pageable
            )).thenReturn(categoryPage);
            when(mapper.toResponseDto(any(Category.class))).thenReturn(responseDto);

            // WHEN
            Page<CategoryResponseDto> result = categoryService.search(keyword, pageable);

            // THEN
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }
    }

    /**
     * Tests d'intégration entre méthodes
     *
     * Concepts :
     * - Vérifier que les différentes méthodes peuvent travailler ensemble
     * - Tester des scénarios réalistes
     */
    @Nested
    @DisplayName("Tests d'intégration")
    class IntegrationTests {

        @Test
        @DisplayName("Devrait créer puis récupérer une catégorie")
        void shouldCreateThenRetrieveCategory() {
            // GIVEN - Création
            when(repository.existsByNameIgnoreCase(createDto.name())).thenReturn(false);
            when(mapper.createEntity(createDto)).thenReturn(category);
            when(repository.save(category)).thenReturn(category);
            when(mapper.toDto(category)).thenReturn(responseDto);

            // WHEN - Création
            CategoryResponseDto created = categoryService.create(createDto);

            // THEN
            assertThat(created).isNotNull();
            assertThat(created.id()).isEqualTo(1L);

            // GIVEN - Récupération
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);
            when(repository.findAll(pageable)).thenReturn(categoryPage);
            when(mapper.toDto(category)).thenReturn(responseDto);

            // WHEN - Récupération
            Page<CategoryResponseDto> retrieved = categoryService.getCategories(pageable);

            // THEN
            assertThat(retrieved.getContent()).hasSize(1);
            assertThat(retrieved.getContent().get(0).id()).isEqualTo(created.id());
        }
    }
}