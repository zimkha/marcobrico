package com.mmd.marcobrico.TI;


import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.product.ProductCreateDto;
import com.mmd.marcobrico.mapper.ProductMapper;
import com.mmd.marcobrico.repository.CategoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import org.instancio.Instancio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

public class ProduitIT extends AbstractIT {

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductMapper productMapper;
    Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(
            Category.create("Electronics-" + UUID.randomUUID(), "Electronic devices" + UUID.randomUUID())
        );
    }

    @Test
    void shouldSaveAndFindProduct() {

        var product = Instancio.of(ProductCreateDto.class)
            .set(field(ProductCreateDto::categoryId), category.getId())
            .create();
        var productSaved = productRepository.save(productMapper.toEntity(product, category));
        assertThat(productRepository.findById(productSaved.getId())).isPresent();
    }

    @Test
    void shouldRejectProduct_whenCategoryDoesNotExist() {
        var ghostCategory = categoryRepository.findById(-999L);
        assertThat(ghostCategory).isEmpty();
        var fakeCategory = Category.builder()
            .id(-999L)
            .build();
        var productDto = Instancio.of(ProductCreateDto.class).create();

        assertThatThrownBy(() ->
            productRepository.save(productMapper.toEntity(productDto, fakeCategory))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
