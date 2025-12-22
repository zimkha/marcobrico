package com.mmd.marcobrico.service.impl;


import com.mmd.marcobrico.domain.Delivery;
import com.mmd.marcobrico.domain.DeliveryStatus;
import com.mmd.marcobrico.domain.Partner;
import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateDto;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.mapper.DeliveryMapper;
import com.mmd.marcobrico.repository.DeliveryRepository;
import com.mmd.marcobrico.repository.PartnerRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import com.mmd.marcobrico.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final SaleRepository saleRepository;
    private final PartnerRepository partnerRepository;
    private final DeliveryMapper mapper;

    @Override
    public DeliveryResponseDto create(DeliveryCreateDto dto) {

        Sale sale = saleRepository.findById(dto.saleId())
                .orElseThrow(() -> new BusinessException("Vente introuvable"));

        Partner carrier = partnerRepository.findById(dto.carrierId())
                .orElseThrow(() -> new BusinessException("Transporteur introuvable"));

        Delivery delivery = Delivery.create(sale, carrier);
        return mapper.toDto(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryResponseDto dispatch(Long deliveryId, String trackingNumber) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("Livraison introuvable"));

        Delivery dispatched = delivery.markInTransit(trackingNumber);
        return mapper.toDto(deliveryRepository.save(dispatched));
    }

    @Override
    public DeliveryResponseDto deliver(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("Livraison introuvable"));

        Delivery delivered = delivery.markDelivered();
        return mapper.toDto(deliveryRepository.save(delivered));
    }

    @Override
    public DeliveryResponseDto cancel(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("Livraison introuvable"));

        Delivery canceled = delivery.cancel();
        return mapper.toDto(deliveryRepository.save(canceled));
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

}
