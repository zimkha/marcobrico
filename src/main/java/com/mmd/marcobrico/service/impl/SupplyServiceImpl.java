package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.*;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.dto.supply.SupplyCreateDto;
import com.mmd.marcobrico.dto.supply.SupplyResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.mapper.SupplyMapper;
import com.mmd.marcobrico.repository.InventoryRepository;
import com.mmd.marcobrico.repository.PartnerRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.SupplyRepository;
import com.mmd.marcobrico.service.SupplyService;
import com.mmd.marcobrico.service.jwt.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyServiceImpl implements SupplyService {

    private final SupplyRepository supplyRepository;
    private final PartnerRepository partnerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplyMapper mapper;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    public SupplyResponseDto create(SupplyCreateDto dto) {

        Partner supplier = partnerRepository.findById(dto.supplierId())
                .orElseThrow(() -> new BusinessException("Fournisseur introuvable"));

        if (supplier.getType() != PartnerType.SUPPLIER)
            throw new BusinessException("Partenaire non fournisseur");

        List<SupplyItem> items = dto.items().stream().map(i -> {
            Product product = productRepository.findById(i.productId())
                    .orElseThrow(() -> new BusinessException("Produit introuvable"));

            return SupplyItem.create(product, i.quantity(),i.price());
        }).toList();

        Supply supply = Supply.create(supplier, items);
        return mapper.toDto(supplyRepository.save(supply));
    }

    @Override
    public SupplyResponseDto receive(Long supplyId) {
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new BusinessException("Approvisionnement introuvable"));

        if (supply.getStatus() != SupplyStatus.CREATED)
            throw new BusinessException("Approvisionnement non recevable");

        supply.getItems().forEach(item -> {
            Product product = item.getProduct();
            int before = product.getQuantity();

            int after = before + item.getQuantity();
            productRepository.save(product.changeQuantity(after));

            var user = authenticatedUserService.getUserConnected();
            inventoryRepository.save(
                    InventoryEntry.create(
                            product,
                            before,
                            after,
                            InventoryType.ENTRY,
                            "Réception approvisionnement",
                            user
                    )
            );
        });

        Supply received = supply.markAsReceived();
        return mapper.toDto(supplyRepository.save(received));
    }

    @Override
    public SupplyResponseDto cancel(Long supplyId) {

        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new BusinessException("Approvisionnement introuvable"));

        Supply canceled = supply.cancel();
        return mapper.toDto(supplyRepository.save(canceled));
    }

    @Override
    public Page<SupplyResponseDto> search(Long supplierId, SupplyStatus status, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Specification<Supply> spec = (root, query, cb) -> {
            var predicates = new ArrayList<>();

            if (supplierId != null)
                predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return supplyRepository.findAll(spec, pageable).map(mapper::toDto);
    }

    @Override
    public DeliveryResponseDto createDeliveryFromSupply(Long supplyId, Long clientId, String address) {
        return null;
    }

}
