package com.homefit.app.data.remote.dto;

public class UpdateProfileRequest {
    public final String displayName;
    public final Double heightCm;
    public final Double weightKg;

    public UpdateProfileRequest(String displayName, Double heightCm, Double weightKg) {
        this.displayName = displayName;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }
}
