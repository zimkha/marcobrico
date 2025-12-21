package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto create(CategoryCreateDto dto);
    CategoryResponseDto update(Long id, CategoryUpdateDto dto);
}
