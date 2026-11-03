package com.homefit.server.service;

import com.homefit.server.api.ApiException;
import com.homefit.server.api.dto.UpdateProfileRequest;
import com.homefit.server.api.dto.UserResponse;
import com.homefit.server.api.dto.StatsResponse;
import com.homefit.server.domain.User;
import com.homefit.server.repository.UserRepository;
import com.homefit.server.repository.WorkoutSessionRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Hồ sơ người dùng. Luôn lọc theo userId lấy từ token, không nhận userId từ client. */
@Service
public class UserService {

    private final UserRepository users;
    private final WorkoutSessionRepository sessions;

    public UserService(UserRepository users, WorkoutSessionRepository sessions) {
        this.users = users;
        this.sessions = sessions;
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

    @Transactional(readOnly = true)
    public StatsResponse getStats(Long userId) {
        int total = sessions.countByUserId(userId);
        long duration = sessions.sumDurationByUserId(userId);
        
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        
        int last7 = sessions.countByUserIdSince(userId, sevenDaysAgo);
        int last30 = sessions.countByUserIdSince(userId, thirtyDaysAgo);
        
        List<LocalDate> distinctDays = sessions.findDistinctWorkoutDaysDesc(userId);
        int streak = 0;
        
        if (!distinctDays.isEmpty()) {
            LocalDate today = LocalDate.now(ZoneId.of("UTC"));
            LocalDate current = distinctDays.get(0);
            
            // Streak bắt đầu đếm nếu buổi tập cuối cùng là hôm nay hoặc hôm qua
            if (current.equals(today) || current.equals(today.minusDays(1))) {
                streak = 1;
                for (int i = 1; i < distinctDays.size(); i++) {
                    if (distinctDays.get(i).equals(current.minusDays(1))) {
                        streak++;
                        current = distinctDays.get(i);
                    } else {
                        break;
                    }
                }
            }
        }
        
        return new StatsResponse(total, duration, streak, last7, last30);
    }

    private User find(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Tài khoản không còn tồn tại."));
    }

    private static Double roundOneDecimal(Double value) {
        return value == null ? null : Math.round(value * 10.0) / 10.0;
    }
}
