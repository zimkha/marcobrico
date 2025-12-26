package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.InventoryEntry;
import com.mmd.marcobrico.domain.InventoryType;
import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.User;
import com.mmd.marcobrico.dto.inventory.InventoryCreateDto;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.InventoryMapper;
import com.mmd.marcobrico.repository.InventoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.service.InventoryService;
import com.mmd.marcobrico.service.jwt.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

        // Recupere l'utilisateuyr connecter
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
}
