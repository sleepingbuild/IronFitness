package com.homefit.app.data.remote.dto;

public class CreateOrderRequestDto {
    public String packageType; // "MONTHLY" or "YEARLY"
    public long amount;

    public CreateOrderRequestDto() {}

    public CreateOrderRequestDto(String packageType, long amount) {
        this.packageType = packageType;
        this.amount = amount;
    }
}
