package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Category;
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
import lombok.extern.slf4j.Slf4j;
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
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;
    private final SaleRepository saleRepository;

    @Override
    public ProductResponseDto create(ProductCreateDto dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catégorie non trouvée avec l'ID: " + dto.categoryId()));
        Product product = mapper.toEntity(dto, category);

        Product savedProduct = repository.save(product);

        log.debug("Produit créé avec ID: {}", savedProduct.getId());

        return mapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDto update(Long id, ProductUpdateDto dto) {
        log.info("Mise à jour du produit ID: {}", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + id));

        Category category = null;
        if (dto.categoryId() != null) {
            category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Catégorie non trouvée avec l'ID: " + dto.categoryId()));
        }

        mapper.updateEntity(dto, product, category);

        log.debug("Produit mis à jour: {}", product.getId());

        return mapper.toDto(product);
    }

    @Override
    public ProductResponseDto changeQuantity(Long id, int newQuantity) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

       // Product updated = existing.changeQuantity(newQuantity);
        return mapper.toDto(repository.save(existing));
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Page<ProductResponseDto> findByCategory(Long categoryId, Pageable pageable) {
        return null;
    }

    @Override
    public ProductResponseDto findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
        return mapper.toDto(product);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Produit non trouvé avec l'ID: " + id);
        }

        repository.deleteById(id);

        log.info("Produit {} supprimé définitivement", id);
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
//        Product product = repository.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));
//
//        long numberOfSales = saleRepository.countByProductId(productId);
//
//        Sale lastSale = saleRepository.findSalesByProductIdOrderByDateDesc(productId)
//                .stream()
//                .findFirst()
//                .orElse(null);
//
//        BigDecimal revenue = saleRepository.getRevenueByProductId(productId);
//        if (revenue == null) revenue = BigDecimal.ZERO;
//
//        return new ProductDetailDto(
//                product.getId(),
//                product.getName(),
//
//                numberOfSales,
//
//                revenue
//        );
        return null;
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
                product.getBaseUnit(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.isActive(),
                lastSales
        );
    }

    @Override
    public ProductResponseDto findByReference(String reference) {

        Product product = repository.findByReference(reference).orElseThrow(
                () -> new ResourceNotFoundException("Produit  on trouvé:" +reference));

        return mapper.toDto(product);
    }

    @Override
    public Page<ProductResponseDto> searchByName(String name, Pageable pageable) {
        log.debug("Recherche produits par nom: {}", name);

        return repository.findByNameContainingIgnoreCase(name, pageable).map(mapper::toDto);
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

    @Override
    public Page<ProductResponseDto> findAllActive(Pageable pageable) {
        return repository.findByActiveTrue(pageable).map(mapper::toDto);
    }

    @Override
    public ProductResponseDto setActive(Long id, boolean active) {
        log.info("Changement statut produit ID: {} → {}", id, active);

        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + id));
        product.setActive(active);
        return mapper.toDto(product);
    }
}
