package com.homefit.server.service;

import com.homefit.server.api.ApiException;
import com.homefit.server.api.dto.UpdateProfileRequest;
import com.homefit.server.api.dto.UserResponse;
import com.homefit.server.domain.User;
import com.homefit.server.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Hồ sơ người dùng. Luôn lọc theo userId lấy từ token, không nhận userId từ client. */
@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return UserResponse.from(find(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = find(userId);
        user.setDisplayName(request.displayName().trim());
        user.setHeightCm(roundOneDecimal(request.heightCm()));
        user.setWeightKg(roundOneDecimal(request.weightKg()));
        return UserResponse.from(users.save(user));
    }

    private User find(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Tài khoản không còn tồn tại."));
    }

    private static Double roundOneDecimal(Double value) {
        return value == null ? null : Math.round(value * 10.0) / 10.0;
    }
}
