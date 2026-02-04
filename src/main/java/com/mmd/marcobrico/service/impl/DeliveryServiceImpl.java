package com.mmd.marcobrico.service.impl;


import com.mmd.marcobrico.domain.*;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateDto;
import com.mmd.marcobrico.dto.delivery.DeliveryItemCreateDto;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.exception.ResourceNotFoundException;
import com.mmd.marcobrico.mapper.DeliveryMapper;
import com.mmd.marcobrico.repository.*;
import com.mmd.marcobrico.service.DeliveryService;
import com.mmd.marcobrico.service.InventoryService;
import com.mmd.marcobrico.service.jwt.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;
    private final SupplyRepository supplyRepository;
    private final DeliveryMapper mapper;
    private final ClientRepository clientRepository;
    private final SaleRepository saleRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    @Override
    public DeliveryResponseDto create(DeliveryCreateDto dto) {
        Client client = clientRepository.findById(dto.clientId())
                .orElseThrow(() -> new BusinessException("Client introuvable"));


        Delivery delivery = Delivery.create(client, dto.address());

        for (DeliveryItemCreateDto itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new BusinessException("Produit introuvable"));

            DeliveryItem item = DeliveryItem.create(
                    delivery,
                    product,
                    itemDto.quantity()
            );

            delivery.addItem(item);
        }

        return mapper.toDto(deliveryRepository.save(delivery));
    }


    @Override
    public DeliveryResponseDto dispatch(Long deliveryId, String trackingNumber) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable"));

        delivery.markInTransit(trackingNumber);

        return mapper.toDto(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryResponseDto deliver(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable"));

        delivery.markDelivered();

        List<SaleItem> saleItems = new ArrayList<>();
        for (DeliveryItem item : delivery.getItems()) {
            Product product = item.getProduct();
//            int beforeQty = product.getQuantity();
//
//            int afterQty = beforeQty - item.getQuantityDelivered();
//            if (afterQty < 0) throw new BusinessException("Stock insuffisant pour " + product.getName());
           // product.changeQuantity(afterQty);
            productRepository.save(product);


            inventoryRepository.save(
                    InventoryEntry.create(
                            product,
                            0,
                            10,
                            InventoryType.SALE,
                            "Livraison n°" + delivery.getId(),
                            authenticatedUserService.getUserConnected()
                    )
            );


            SaleItem saleItem = SaleItem.create(product, item.getQuantityDelivered(), item.getSalePrice());
            saleItems.add(saleItem);
        }

        Sale sale = Sale.createFromDelivery(authenticatedUserService.getUserConnected(), saleItems, delivery.getClient(), delivery);

        saleRepository.save(sale);

        return mapper.toDto(deliveryRepository.save(delivery));

    }

    @Override
    public DeliveryResponseDto cancel(Long deliveryId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable"));

        delivery.cancel();

        return mapper.toDto(deliveryRepository.save(delivery));
    }


    @Override
    public DeliveryResponseDto createDeliveryFromReceivedSupply(Long supplyId, Long clientId, String address) {
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Approvisionnement introuvable"));

        if (supply.getStatus() != SupplyStatus.RECEIVED) {
            throw new BusinessException("Approvisionnement non reçu");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable"));

        Delivery delivery = Delivery.create(client,  address);

        for (SupplyItem si : supply.getItems()) {
            DeliveryItem di = DeliveryItem.create(delivery, si.getProduct(), si.getQuantity());
            delivery.addItem(di);
        }

        return mapper.toDto(deliveryRepository.save(delivery));
    }

    @Override
    public Page<DeliveryResponseDto> search(Long saleId, Long carrierId, DeliveryStatus status, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Specification<Delivery> spec = (root, query, cb) -> {
            var predicates = new ArrayList<>();

            if (saleId != null)
                predicates.add(cb.equal(root.get("sale").get("id"), saleId));

            if (carrierId != null)
                predicates.add(cb.equal(root.get("carrier").get("id"), carrierId));

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return deliveryRepository.findAll(spec, pageable).map(mapper::toDto);
    }

    @Override
    public List<DeliveryResponseDto> deliveries() {
        return deliveryRepository.findAll().stream()
                .filter(delivery -> delivery.getStatus() != DeliveryStatus.CANCELED)
                .map(mapper::toDto)
                .toList();
    }

    private List<SaleItem> createSaleItemsFromDelivery(Delivery delivery) {
        List<SaleItem> saleItems = new ArrayList<>();

        for (DeliveryItem item : delivery.getItems()) {

            inventoryService.deductStock(
                    item.getProduct(),
                    item.getQuantityDelivered(),
                    "Livraison n°" + delivery.getId()
            );

            // Créer le SaleItem
            SaleItem saleItem = SaleItem.create(
                    item.getProduct(),
                    item.getQuantityDelivered(),
                    item.getSalePrice()
            );
            saleItems.add(saleItem);
        }

        return saleItems;
    }

}
