package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Partner;
import com.mmd.marcobrico.domain.PartnerType;
import com.mmd.marcobrico.dto.partner.PartnerCreateDto;
import com.mmd.marcobrico.dto.partner.PartnerResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.mapper.PartnerMapper;
import com.mmd.marcobrico.repository.PartnerRepository;
import com.mmd.marcobrico.service.PartnerService;
import com.mmd.marcobrico.specification.PartnerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository repository;
    private final PartnerMapper mapper;


    @Override
    public PartnerResponseDto create(PartnerCreateDto dto) {
        if (dto.phone() != null && repository.existsByPhone(dto.phone()))
            throw new BusinessException("Téléphone déjà utilisé");

        if (dto.email() != null && repository.existsByEmail(dto.email()))
            throw new BusinessException("Email déjà utilisé");

        Partner partner = Partner.create(
                dto.name(),
                dto.type(),
                dto.phone(),
                dto.email(),
                dto.address()
        );

        return mapper.toDto(repository.save(partner));
    }

    @Override
    public Page<PartnerResponseDto> search(String name, PartnerType type, String phone, String email, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        return repository
                .findAll(PartnerSpecification.search(name, type, phone, email), pageable)
                .map(mapper::toDto);
    }

}
