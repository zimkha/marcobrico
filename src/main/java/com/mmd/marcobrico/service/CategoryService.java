package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto create(CategoryCreateDto dto);
    CategoryResponseDto update(Long id, CategoryUpdateDto dto);
    Page<CategoryResponseDto> getCategories(Pageable pageable);
    Page<CategoryResponseDto> search(String keyword, Pageable pageable);
}
