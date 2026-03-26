package com.mmd.marcobrico.mapper;

import com.mmd.marcobrico.domain.Payment;
import com.mmd.marcobrico.dto.payment.PaymentResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default PaymentResponseDto toDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getReference(),
                payment.getCreatedAt()
        );
    }
}
