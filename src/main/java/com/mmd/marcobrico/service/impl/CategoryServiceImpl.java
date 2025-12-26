package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.CategoryMapper;
import com.mmd.marcobrico.repository.CategoryRepository;
import com.mmd.marcobrico.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryServiceImpl(CategoryRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CategoryResponseDto create(CategoryCreateDto dto) {

        if (repository.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Catégorie déjà existante");
        }

        Category category = mapper.createEntity(dto);

        return mapper.toDto(repository.save(category));
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryUpdateDto dto) {

        Category existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

        Category updated = mapper.updateEntity(dto, existing);

        return mapper.toDto(repository.save(updated));
    }

    @Override
    public Page<CategoryResponseDto> getCategories(Pageable pageable) {
        return repository
                .findAll(pageable)
                .map(mapper::toDto);
    }

    @Override
    public Page<CategoryResponseDto> search(String keyword, Pageable pageable) {
        Page<Category> categories = repository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        return categories.map(mapper::toResponseDto);
    }

}
