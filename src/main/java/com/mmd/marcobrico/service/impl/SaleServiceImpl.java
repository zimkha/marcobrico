package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.*;
import com.mmd.marcobrico.dto.sale.SaleCreateDto;
import com.mmd.marcobrico.dto.sale.SaleResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.SaleMapper;
import com.mmd.marcobrico.repository.InventoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import com.mmd.marcobrico.repository.UserRepository;
import com.mmd.marcobrico.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleMapper saleMapper;
    private final UserRepository userRepository;

    @Override
    public SaleResponseDto createSale(SaleCreateDto dto) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        var user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        List<SaleItem> items = dto.items().stream().map(i -> {
            var product = productRepository.findById(i.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

            int newQuantity = product.getQuantity() - i.quantity();
            if (newQuantity < 0) throw new BusinessException("Stock insuffisant pour " + product.getName());


            productRepository.save(product.changeQuantity(newQuantity));

            InventoryEntry entry = InventoryEntry.create(
                    product,
                    product.getQuantity(),
                    newQuantity,
                    InventoryType.SALE,
                    "Vente",
                    user

            );
            inventoryRepository.save(entry);

            return SaleItem.create(product, i.quantity());
        }).toList();

        Sale entry = Sale.create(user, items);
        return saleMapper.toDto(saleRepository.save(entry));
    }


    @Override
    public SaleResponseDto cancelSale(Long saleId, String comment) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        var currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable"));

        if (sale.isCanceled()) throw new BusinessException("Vente déjà annulée");

        sale.getItems().forEach(item -> {
            Product product = item.getProduct();
            int restoredQuantity = product.getQuantity() + item.getQuantity();

            productRepository.save(product.changeQuantity(restoredQuantity));

            InventoryEntry canceled = InventoryEntry.create(
                    product,
                    product.getQuantity(),
                    restoredQuantity,
                    InventoryType.CANCELED,
                    "Annulation vente: " + comment, currentUser
            );
            inventoryRepository.save(canceled);
        });

        Sale canceledSale = sale.cancel();
        return saleMapper.toDto(saleRepository.save(canceledSale));
    }

    @Override
    public Page<SaleResponseDto> searchSales(
            Long productId,
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Specification<Sale> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null)
                predicates.add(cb.equal(root.get("user").get("id"), userId));

            LocalDateTime startDateTime = startDate != null
                    ? startDate.atStartOfDay()
                    : null;

            LocalDateTime endDateTime = endDate != null
                    ? endDate.atTime(LocalTime.MAX)
                    : null;
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));

            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));

            if (productId != null) {
                Join<Sale, SaleItem> items = root.join("items");
                predicates.add(cb.equal(items.get("product").get("id"), productId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Sale> sales = saleRepository.findAll(spec, pageable);
        return sales.map(saleMapper::toDto);
    }

    @Override
    public SaleResponseDto getSaleDetail(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vente non trouvé"));
        return saleMapper.toDto(sale);
    }
}


