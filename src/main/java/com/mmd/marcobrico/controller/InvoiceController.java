package com.mmd.marcobrico.controller;


import com.mmd.marcobrico.dto.invoice.InvoicePaymentDto;
import com.mmd.marcobrico.dto.invoice.InvoiceResponseDto;
import com.mmd.marcobrico.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping("/sale/{saleId}")
    public InvoiceResponseDto issue(@PathVariable Long saleId) {
        return service.issue(saleId);
    }

    @PostMapping("/{id}/pay")
    public InvoiceResponseDto pay(
            @PathVariable Long id,
            @RequestBody InvoicePaymentDto dto
    ) {
        return service.pay(id, dto.amount());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {

        byte[] pdf = service.generatePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=facture.pdf")
                .body(pdf);
    }
}
