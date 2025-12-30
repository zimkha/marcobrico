package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.domain.DeliveryStatus;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateDto;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateSaleRequestDto;
import com.mmd.marcobrico.dto.delivery.DeliveryDispatchDto;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Validated
public class DeliveryController {

    private final DeliveryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DeliveryResponseDto> create(
            @Valid @RequestBody DeliveryCreateDto dto
    ) {
        DeliveryResponseDto response = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/v1/deliveries/" + response.id()))
                .body(response);
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<DeliveryResponseDto> dispatch(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryDispatchDto dto
    ) {
        DeliveryResponseDto response = service.dispatch(id, dto.trackingNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<DeliveryResponseDto> deliver(@PathVariable Long id) {
        DeliveryResponseDto response = service.deliver(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DeliveryResponseDto>> search(
            @RequestParam(required = false) Long saleId,
            @RequestParam(required = false) Long carrierId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Page<DeliveryResponseDto> results = service.search(
                saleId, carrierId, status, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(results);
    }

    @PostMapping("/create-from-supply")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DeliveryResponseDto> createDeliveryFromReceivedSupply(
            @Valid @RequestBody DeliveryCreateSaleRequestDto dto
    ) {
        DeliveryResponseDto response = service.createDeliveryFromReceivedSupply(
                dto.supplyId(),
                dto.clientId(),
                dto.address()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/deliveries/" + response.id()))
                .body(response);
    }
}
