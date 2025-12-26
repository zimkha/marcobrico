package com.mmd.marcobrico.controller;

import com.mmd.marcobrico.dto.product.*;
import com.mmd.marcobrico.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ProductResponseDto create(@RequestBody @Valid ProductCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ProductResponseDto update(@PathVariable Long id, @RequestBody @Valid ProductUpdateDto dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/quantity")
    public ProductResponseDto changeQuantity(@PathVariable Long id, @RequestParam int newQuantity) {
        return service.changeQuantity(id, newQuantity);
    }

    @GetMapping
    public List<ProductResponseDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}/details")
    public ProductResponseDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @GetMapping("/search")
    public Page<ProductResponseDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean belowThreshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        var filter = new ProductFilterDto(name, categoryId, belowThreshold, page, size, sortBy, sortDirection);
        return service.searchProducts(filter);
    }
    @GetMapping("/{id}/stats")
    public ProductStatsDto getProductStats(@PathVariable("id") Long productId) {
        return service.getProductStats(productId);
    }
}
