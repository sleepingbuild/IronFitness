package com.homefit.server.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** PUT /me/profile. heightCm và weightKg được phép null (xóa số đo). */
public record UpdateProfileRequest(
        @NotBlank(message = "Vui lòng nhập tên hiển thị")
        @Size(max = 100, message = "Tên hiển thị tối đa 100 ký tự")
        String displayName,

        @DecimalMin(value = "50.0", message = "Chiều cao phải từ 50 đến 250 cm")
        @DecimalMax(value = "250.0", message = "Chiều cao phải từ 50 đến 250 cm")
        Double heightCm,

        @DecimalMin(value = "20.0", message = "Cân nặng phải từ 20 đến 300 kg")
        @DecimalMax(value = "300.0", message = "Cân nặng phải từ 20 đến 300 kg")
        Double weightKg) {
}
