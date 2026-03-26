package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.domain.Category;
import com.mmd.marcobrico.dto.category.CategoryCreateDto;
import com.mmd.marcobrico.dto.category.CategoryResponseDto;
import com.mmd.marcobrico.dto.category.CategoryUpdateDto;
import com.mmd.marcobrico.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    @Operation(summary = "Create one Category", description = "description")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "HttpStatus CREATED"),
            @ApiResponse(responseCode = "500", description = "HttpStatus Internal Server Error")})
    public CategoryResponseDto create(@RequestBody @Valid CategoryCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one Category", description = "description")
    public CategoryResponseDto update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryUpdateDto dto
    ) {
        return service.update(id, dto);
    }

    @GetMapping
    public Page<CategoryResponseDto> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getCategories(pageable);
    }
    @GetMapping("/search")
    public Page<CategoryResponseDto> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.search(keyword, pageable);
    }


}
