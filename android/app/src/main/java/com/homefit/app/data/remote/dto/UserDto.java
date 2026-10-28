package com.homefit.app.data.remote.dto;

/** Người dùng, cùng cấu trúc với GET /me. */
public class UserDto {
    public long id;
    public String email;
    public String displayName;
    public Boolean isPremium;
    public Double heightCm;
    public Double weightKg;
}
