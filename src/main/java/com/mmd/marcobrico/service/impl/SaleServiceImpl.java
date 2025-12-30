package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.*;
import com.mmd.marcobrico.dto.sale.SaleCreateDto;
import com.mmd.marcobrico.dto.sale.SaleItemDto;
import com.mmd.marcobrico.dto.sale.SaleResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.SaleMapper;
import com.mmd.marcobrico.repository.InventoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import com.mmd.marcobrico.repository.UserRepository;
import com.mmd.marcobrico.service.InventoryService;
import com.mmd.marcobrico.service.SaleService;
import com.mmd.marcobrico.service.jwt.AuthenticatedUserService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final InventoryService inventoryService;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    public SaleResponseDto createSale(SaleCreateDto dto) {

        var user = authenticatedUserService.getUserConnected();

        var items = dto.items().stream()
                .map(this::createSaleItem)
                .toList();


        Sale entry = Sale.createSimple(user, items);
        return saleMapper.toDto(saleRepository.save(entry));
    }

    @Override
    public SaleResponseDto createFromDelivery(Delivery delivery) {
        List<SaleItem> saleItems = createSaleItemsFromDelivery(delivery);
        Sale sale = Sale.createFromDelivery(
                authenticatedUserService.getUserConnected(),
                saleItems,
                delivery.getClient(),
                delivery
        );

        return saleMapper.toDto(saleRepository.save(sale));

    }



    @Override
    public SaleResponseDto cancelSale(Long saleId, String comment) {

        var user = authenticatedUserService.getUserConnected();

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable"));

        if (sale.isCanceled()) {
            throw new BusinessException("Vente déjà annulée");
        }

        restoreInventoryForSaleCancellation(sale, comment);

        sale.cancel();
        return saleMapper.toDto(saleRepository.save(sale));
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


    // PRIVATE METHODE
    private List<SaleItem> createSaleItemsFromDelivery(Delivery delivery) {
        List<SaleItem> saleItems = new ArrayList<>();
        for (DeliveryItem item : delivery.getItems()) {
            inventoryService.deductStock(
                    item.getProduct(),
                    item.getQuantityDelivered(),
                    "Livraison n°" + delivery.getId()
            );

            SaleItem saleItem = SaleItem.create(
                    item.getProduct(),
                    item.getQuantityDelivered(),
                    item.getSalePrice()
            );
            saleItems.add(saleItem);
        }

        return saleItems;
    }

    private SaleItem createSaleItem(SaleItemDto itemDto) {
        var product = productRepository.findById(itemDto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        validateStock(product, itemDto.quantity());
        var reason = "Vente";
        inventoryService.deductStock(product, itemDto.quantity(), reason);

        var totalSub = itemDto.price().multiply(BigDecimal.valueOf(itemDto.quantity()));

        return SaleItem.create(product, itemDto.quantity(), totalSub);
    }

    private void validateStock(Product product, int quantity) {
        int newQuantity = product.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new BusinessException("Stock insuffisant pour " + product.getName());
        }
    }
    private void restoreInventoryForItemCancellation(SaleItem item, String comment) {
        Product product = item.getProduct();

        int newQuantity = product.getQuantity() + item.getQuantity();

        String reason = "Annulation vente: " + comment;

        inventoryService.applyInventoryAdjustment(product, newQuantity, reason);
    }

    private void restoreInventoryForSaleCancellation(Sale sale, String comment) {
        sale.getItems().forEach(item -> restoreInventoryForItemCancellation(item, comment));
    }

}


