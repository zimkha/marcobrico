package com.mmd.marcobrico.dto.aging;

import lombok.Getter;

import java.math.BigDecimal;


@Getter
public class AgingBucketDto {

    private final String label;
    private final BigDecimal amount;

    public AgingBucketDto(String label, BigDecimal amount) {
        this.label = label;
        this.amount = amount;
    }

}
