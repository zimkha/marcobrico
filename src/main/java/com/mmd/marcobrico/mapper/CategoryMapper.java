package com.mmd.marcobrico.mapper;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoryMapper {
    @ObjectFactory
    default Category createEntity(CategoryCreateDto dto) {
        return Category.create(dto.name(), dto.description());
    }

    @ObjectFactory
    default Category updateEntity(
            CategoryUpdateDto dto,
            @Context Category existing
    ) {
        return existing.update(dto.name(), dto.description());
    }

    CategoryResponseDto toDto(Category category);
}
