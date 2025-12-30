package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.InventoryEntry;
import com.mmd.marcobrico.domain.InventoryType;
import com.mmd.marcobrico.domain.Product;

import com.mmd.marcobrico.dto.inventory.InventoryCreateDto;
import com.mmd.marcobrico.dto.inventory.InventoryFilterDto;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.InventoryMapper;
import com.mmd.marcobrico.repository.InventoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.service.InventoryService;
import com.mmd.marcobrico.service.jwt.AuthenticatedUserService;
import com.mmd.marcobrico.specification.InventorySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper mapper;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    public InventoryResponseDto addEntry(InventoryCreateDto dto) {

        var product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        int quantityBefore = product.getQuantity();
        int quantityAfter = quantityBefore + dto.quantityChange();

        if (quantityAfter < 0) {
            throw new BusinessException("Stock insuffisant pour cette opération");
        }
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();


        var type = dto.quantityChange() > 0 ? InventoryType.ENTRY : InventoryType.SALE;
        var  user = authenticatedUserService.getUserConnected();
        var entry = InventoryEntry.create(product, quantityBefore, quantityAfter, type, dto.comment(), user);

        productRepository.save(product.changeQuantity(quantityAfter));

        return mapper.toDto(inventoryRepository.save(entry));
    }

    @Override
    public List<InventoryResponseDto> getHistory(Long productId) {
        return inventoryRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    /**
     *
     * @param inventoryId
     * @param userId
     * @param comment
     * @return
     */
    @Override
    public InventoryResponseDto reverseMovement(Long inventoryId, Long userId, String comment) {
        InventoryEntry entry = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Mouvement introuvable"));

        Product product = entry.getProduct();

        int reversedQuantity = entry.getQuantityBefore() - entry.getQuantityAfter();

        InventoryType reverseType = switch (entry.getType()) {
            case ENTRY -> InventoryType.ADJUSTMENT;
            case SALE -> InventoryType.ENTRY;
            case ADJUSTMENT -> InventoryType.ADJUSTMENT;
            case CANCELED -> InventoryType.CANCELED;
        };

        var  user = authenticatedUserService.getUserConnected();
        InventoryEntry reversedEntry = InventoryEntry.create(
                product,
                product.getQuantity(),
                product.getQuantity() + reversedQuantity,
                reverseType,
                comment,
                user
        );


        productRepository.save(product.changeQuantity(product.getQuantity() + reversedQuantity));

        return mapper.toDto(inventoryRepository.save(reversedEntry));
    }

    @Override
    public Page<InventoryResponseDto> search(InventoryFilterDto dto) {

        Pageable pageable = PageRequest.of(
                dto.page(),
                dto.size(),
                Sort.by(Sort.Direction.fromString(dto.sortDirection()), dto.sortBy())
        );

        Page<InventoryEntry> page = inventoryRepository.findAll(
                InventorySpecification.filter(dto),
                pageable
        );

        return page.map(mapper::toDto);
    }

    @Override
    public void deductStock(Product product, int quantity, String reason) {
        int beforeQty = product.getQuantity();
        int afterQty = beforeQty - quantity;

        if (afterQty < 0) {
            throw new BusinessException(
                    String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                            product.getName(), beforeQty, quantity)
            );
        }

        Product updatedProduct = product.changeQuantity(afterQty);
        productRepository.save(updatedProduct);

        createInventoryEntry(updatedProduct, beforeQty, afterQty, InventoryType.SALE, reason);
    }

    @Override
    public void addStock(Product product, int quantity, String reason) {
        int beforeQty = product.getQuantity();
        int afterQty = beforeQty + quantity;

        Product updatedProduct = product.changeQuantity(afterQty);
        productRepository.save(updatedProduct);

        createInventoryEntry(updatedProduct, beforeQty, afterQty, InventoryType.ENTRY, reason);
    }

    @Override
    public void applyInventoryAdjustment(Product product, int newQuantity, String reason) {
        int beforeQty = product.getQuantity();

        Product updatedProduct = product.changeQuantity(newQuantity);
        productRepository.save(updatedProduct);

        int afterQty = updatedProduct.getQuantity();

        createInventoryEntry(updatedProduct, beforeQty, afterQty, InventoryType.ADJUSTMENT, reason);
    }

    private void createInventoryEntry(Product product, int beforeQty, int afterQty,
                                      InventoryType type, String reason) {
        InventoryEntry entry = InventoryEntry.create(
                product,
                beforeQty,
                afterQty,
                type,
                reason,
                authenticatedUserService.getUserConnected()
        );
        inventoryRepository.save(entry);
    }

}
