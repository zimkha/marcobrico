package com.mmd.marcobrico.mapper;


import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CategoryMapperTest {

    private CategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CategoryMapperImpl();
    }
    @Test
    void createEntity_shouldCallDomainFactoryMethod() {
        // Given
        CategoryCreateDto dto = Instancio.of(CategoryCreateDto.class)
                .set(field(CategoryCreateDto::name), "Category 1")
                .set(field(CategoryCreateDto::description), "description")
                .create();
        Category result = mapper.createEntity(dto);

        assertEquals("Category 1", result.getName());
    }
    @Test
    void toDto_shouldMapAllFields() {
        Category category = Instancio.create(Category.class);

        CategoryResponseDto result = mapper.toDto(category);

        assertEquals(category.getId(), result.id());
        assertEquals(category.getName(), result.name());

    }

    @Test
    void createdEntityShouldReturnANewEntity(){
        CategoryCreateDto dto = Instancio.create(CategoryCreateDto.class);
        Category result = mapper.createEntity(dto);
        assertEquals(result.getName(), dto.name());
        assertEquals(result.getDescription(), dto.description());
    }
    @Test
    void updateEntityShouldReturnAExistingEntity(){
        Category existing = Instancio.create(Category.class);
        CategoryUpdateDto dto = new CategoryUpdateDto("Name", "Description");
        Category result = mapper.updateEntity(dto, existing);
        assertEquals("Name", result.getName());
    }
}
