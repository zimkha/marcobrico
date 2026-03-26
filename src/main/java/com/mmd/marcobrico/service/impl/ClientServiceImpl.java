package com.mmd.marcobrico.service.impl;


import com.mmd.marcobrico.domain.Client;
import com.mmd.marcobrico.dto.client.ClientCreateDto;
import com.mmd.marcobrico.dto.client.ClientResponseDto;
import com.mmd.marcobrico.exception.BusinessException;
import com.mmd.marcobrico.mapper.ClientMapper;
import com.mmd.marcobrico.repository.ClientRepository;
import com.mmd.marcobrico.service.ClientService;
import com.mmd.marcobrico.specification.ClientSpecification;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;

    @Override
    public ClientResponseDto create(ClientCreateDto dto) {

        checkClient(dto);

        Client client = createClient(dto);

        return mapper.toDto(repository.save(client));
    }

    private static @NonNull Client createClient(ClientCreateDto dto) {
        return Client.create(
                dto.name(),
                dto.phone(),
                dto.email(),
                dto.address()
        );
    }

    private void checkClient(ClientCreateDto dto) {
        if (dto.phone() != null && repository.existsByPhone(dto.phone()))
            throw new BusinessException("Téléphone déjà utilisé");

        if (dto.email() != null && repository.existsByEmail(dto.email()))
            throw new BusinessException("Email déjà utilisé");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDto> search(
            String name,
            String phone,
            String email,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        return repository
                .findAll(ClientSpecification.search(name, phone, email), pageable)
                .map(mapper::toDto);
    }
}
