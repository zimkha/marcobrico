package com.mmd.marcobrico.service;

import com.mmd.marcobrico.dto.client.ClientCreateDto;
import com.mmd.marcobrico.dto.client.ClientResponseDto;
import org.springframework.data.domain.Page;

public interface ClientService {
    ClientResponseDto create(ClientCreateDto dto);

    Page<ClientResponseDto> search(
            String name,
            String phone,
            String email,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
}
