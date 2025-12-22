package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.domain.DeliveryStatus;
import com.mmd.marcobrico.dto.delivery.DeliveryCreateDto;
import com.mmd.marcobrico.dto.delivery.DeliveryDispatchDto;
import com.mmd.marcobrico.dto.delivery.DeliveryResponseDto;
import com.mmd.marcobrico.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService service;

    @PostMapping
    public DeliveryResponseDto create(@RequestBody @Valid DeliveryCreateDto dto) {
        return service.create(dto);
    }

    @PostMapping("/{id}/dispatch")
    public DeliveryResponseDto dispatch(
            @PathVariable Long id,
            @RequestBody DeliveryDispatchDto dto
    ) {
        return service.dispatch(id, dto.trackingNumber());
    }

    @PostMapping("/{id}/deliver")
    public DeliveryResponseDto deliver(@PathVariable Long id) {
        return service.deliver(id);
    }

    @PostMapping("/{id}/cancel")
    public DeliveryResponseDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/search")
    public Page<DeliveryResponseDto> search(
            @RequestParam(required = false) Long saleId,
            @RequestParam(required = false) Long carrierId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return service.search(saleId, carrierId, status, page, size, sortBy, sortDirection);
    }

}
