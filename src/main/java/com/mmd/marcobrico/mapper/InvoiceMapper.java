package com.mmd.marcobrico.mapper;


import com.mmd.marcobrico.domain.Invoice;
import com.mmd.marcobrico.dto.invoice.InvoiceResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {


    default InvoiceResponseDto toDto(Invoice invoice) {
        return new InvoiceResponseDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getSale().getId(),
                invoice.getStatus(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getIssuedAt()
        );
    }
}
