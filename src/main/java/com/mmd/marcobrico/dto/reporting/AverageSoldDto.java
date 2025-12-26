package com.mmd.marcobrico.dto.reporting;

public class AverageSoldDto {

    private final Long productId;
    private final String productName;
    private final Double averageQuantity;

    public AverageSoldDto(Long productId, String productName, Double averageQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.averageQuantity = averageQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getAverageQuantity() {
        return averageQuantity;
    }
}

