package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.dto.product.*;
import com.mmd.marcobrico.dto.sale.SaleItemStatDto;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.ProductMapper;
import com.mmd.marcobrico.repository.CategoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import com.mmd.marcobrico.service.ProductService;
import com.mmd.marcobrico.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;
    private final SaleRepository saleRepository;

    @Override
    public ProductResponseDto create(ProductCreateDto dto) {
        if (repository.existsByReference(dto.reference()))
            throw new IllegalArgumentException("Référence déjà utilisée");

        var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

        Product product = mapper.toEntity(dto, category);
        return mapper.toDto(repository.save(product));
    }

    @Override
    public ProductResponseDto update(Long id, ProductUpdateDto dto) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

        Product updated = mapper.updateEntity(dto, existing, category);
        return mapper.toDto(repository.save(updated));
    }

    @Override
    public ProductResponseDto changeQuantity(Long id, int newQuantity) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        Product updated = existing.changeQuantity(newQuantity);
        return mapper.toDto(repository.save(updated));
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public ProductResponseDto findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
        return mapper.toDto(product);
    }

    @Override
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable")));
    }

    @Override
    public Page<ProductResponseDto> searchProducts(ProductFilterDto filter) {
        Sort.Direction direction = Sort.Direction.fromString(
                filter.sortDirection() == null ? "ASC" : filter.sortDirection()
        );
        String sortBy = filter.sortBy() == null ? "name" : filter.sortBy();

        Pageable pageable = PageRequest.of(filter.page(), filter.size(), Sort.by(direction, sortBy));

        var spec = ProductSpecification.filter(filter.name(), filter.categoryId(), filter.belowThreshold());

        Page<Product> productsPage = repository.findAll(spec, pageable);

        return productsPage.map(mapper::toDto);
    }

    @Override
    public ProductDetailDto getProductDetail(Long productId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

        long numberOfSales = saleRepository.countByProductId(productId);

        Sale lastSale = saleRepository.findSalesByProductIdOrderByDateDesc(productId)
                .stream()
                .findFirst()
                .orElse(null);

        BigDecimal revenue = saleRepository.getRevenueByProductId(productId);
        if (revenue == null) revenue = BigDecimal.ZERO;

        return new ProductDetailDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                numberOfSales,
                lastSale != null ? lastSale.getCreatedAt() : null,
                revenue
        );
    }

    @Override
    public ProductStatsDto getProductStats(Long productId) {
        // Récupération du produit
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

        // Récupération des dernières ventes
        List<SaleItemStatDto> lastSales = saleRepository.findSalesByProductIdOrderByDateDesc(productId)
                .stream()
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct().getId().equals(productId))
                .sorted(Comparator.comparing(item -> item.getSale().getCreatedAt(), Comparator.reverseOrder()))
                .limit(5)
                .map(item -> new SaleItemStatDto(
                        item.getId(),
                        item.getSale().getId(),
                        item.getQuantity(),
                        item.getSale().getCreatedAt()
                ))
                .toList();

        return new ProductStatsDto(
                product.getId(),
                product.getName(),
                product.getReference(),
                product.getPrice(),
                product.getQuantity(),
                product.getSeuilStock(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.isActive(),
                lastSales
        );
    }

    @Override
    public List<ProductResponseDto> findProductByName(String name) {
        if (name == null || name.trim().length() < 2) {
            return List.of();
        }

        return repository
                .findTop10ByNameContainingIgnoreCaseOrderByNameAsc(name.trim())
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
