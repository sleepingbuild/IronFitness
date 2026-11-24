package com.homefit.app.data.remote.dto;

public class GooglePlayVerifyRequestDto {
    public String purchaseToken;
    public String productId;

    public GooglePlayVerifyRequestDto(String purchaseToken, String productId) {
        this.purchaseToken = purchaseToken;
        this.productId = productId;
    }
}
