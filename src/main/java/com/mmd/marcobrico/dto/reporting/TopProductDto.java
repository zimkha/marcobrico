package com.mmd.marcobrico.dto.reporting;


public class TopProductDto {

    private final Long productId;
    private final String productName;
    private final Long totalQuantitySold;

    public TopProductDto(Long productId, String productName, Long totalQuantitySold) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getTotalQuantitySold() {
        return totalQuantitySold;
    }
}