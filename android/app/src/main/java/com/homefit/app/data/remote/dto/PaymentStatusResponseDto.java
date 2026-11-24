package com.homefit.app.data.remote.dto;

public class PaymentStatusResponseDto {
    public String orderCode;
    public String status; // "PENDING", "PAID", "FAILED"
    public boolean isPro;
    public String proExpiryDate;
}
