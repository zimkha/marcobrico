package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.domain.PartnerType;
import com.mmd.marcobrico.dto.partner.PartnerCreateDto;
import com.mmd.marcobrico.dto.partner.PartnerResponseDto;
import com.mmd.marcobrico.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService service;

    @PostMapping
    public PartnerResponseDto create(@RequestBody @Valid PartnerCreateDto dto) {
        return service.create(dto);
    }

    @GetMapping("/search")
    public Page<PartnerResponseDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) PartnerType type,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        return service.search(name, type, phone, email, page, size, sortBy, sortDirection);
    }
}
