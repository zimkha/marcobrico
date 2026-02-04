package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.domain.SupplyStatus;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateSaleRequestDto;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.dto.supply.SupplyCreateDto;
import com.mmd.marcobrico.dto.supply.SupplyResponseDto;
import com.mmd.marcobrico.service.DeliveryService;
import com.mmd.marcobrico.service.SupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService service;
    private final DeliveryService deliveryService;

    @PostMapping
    public SupplyResponseDto create(@RequestBody @Valid SupplyCreateDto dto) {
        return service.create(dto);
    }

    @PostMapping("/{id}/receive")
    public SupplyResponseDto receive(@PathVariable Long id) {
        return service.receive(id);
    }

    @PostMapping("/{id}/cancelation")
    public SupplyResponseDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/search")
    public Page<SupplyResponseDto> search(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) SupplyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return service.search(supplierId, status, page, size, sortBy, sortDirection);
    }
    @PostMapping("/{id}/deliveries")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DeliveryResponseDto> createDeliveryFromReceivedSupply(
            @Valid @RequestBody DeliveryCreateSaleRequestDto dto
    ) {
        DeliveryResponseDto response = deliveryService.createDeliveryFromReceivedSupply(
                dto.supplyId(),
                dto.clientId(),
                dto.address()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/deliveries/" + response.id()))
                .body(response);
    }
}
