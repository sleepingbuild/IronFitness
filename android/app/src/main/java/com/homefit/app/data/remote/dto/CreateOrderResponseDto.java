package com.homefit.app.data.remote.dto;

public class CreateOrderResponseDto {
    public String orderCode;
    public long amount;
    public String status; // "PENDING", "PAID"
    public String bankId;
    public String accountNo;
    public String accountName;
    public String qrUrl;
}
