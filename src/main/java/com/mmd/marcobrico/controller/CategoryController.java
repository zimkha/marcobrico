package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import com.mmd.marcobrico.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    public CategoryResponseDto create(@RequestBody @Valid CategoryCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public CategoryResponseDto update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryUpdateDto dto
    ) {
        return service.update(id, dto);
    }
}
