package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.product.ProductCreateDto;
import com.mmd.marcobrico.dto.product.ProductFilterDto;
import com.mmd.marcobrico.dto.product.ProductResponseDto;
import com.mmd.marcobrico.dto.product.ProductUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponseDto create(ProductCreateDto dto);

    ProductResponseDto update(Long id, ProductUpdateDto dto);

    ProductResponseDto changeQuantity(Long id, int newQuantity);

    List<ProductResponseDto> findAll();

    ProductResponseDto findById(Long id);

    void delete(Long id);
    Page<ProductResponseDto> searchProducts(ProductFilterDto filter);
}
