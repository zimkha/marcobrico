package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.client.ClientCreateDto;
import com.mmd.marcobrico.dto.client.ClientResponseDto;
import com.mmd.marcobrico.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @PostMapping
    public ClientResponseDto create(@RequestBody @Valid ClientCreateDto dto) {
        return service.create(dto);
    }

    @GetMapping("/search")
    public Page<ClientResponseDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        return service.search(name, phone, email, page, size, sortBy, sortDirection);
    }

}
