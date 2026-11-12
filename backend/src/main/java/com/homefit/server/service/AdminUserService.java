package com.homefit.server.service;

import com.homefit.server.domain.Category;
import com.homefit.server.domain.User;
import com.homefit.server.domain.WorkoutSession;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.UserRepository;
import com.homefit.server.repository.WorkoutSessionRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Danh sách người dùng, xem chi tiết và bật/tắt Premium (Premium không thấy quảng cáo). */
@Service
public class AdminUserService {

    public record Row(Long id, String email, String displayName, boolean premium, String createdAtText) {
    }

    public record UserDetail(
            Long id,
            String email,
            String displayName,
            boolean premium,
            Double heightCm,
            Double weightKg,
            Double bmi,
            long totalSessions,
            long totalMinutes,
            long totalExercisesDone,
            String createdAtText,
            List<SessionRow> recentSessions
    ) {}

    public record SessionRow(
            Long id,
            String categoryName,
            int totalSeconds,
            int exercisesDone,
            String completedAtText
    ) {}

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final UserRepository users;
    private final WorkoutSessionRepository sessionRepository;
    private final CategoryRepository categoryRepository;

    public AdminUserService(UserRepository users, WorkoutSessionRepository sessionRepository, CategoryRepository categoryRepository) {
        this.users = users;
        this.sessionRepository = sessionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Row> listUsers() {
        return users.findTop200ByOrderByIdDesc().stream()
                .map(u -> new Row(u.getId(), u.getEmail(), u.getDisplayName(), u.isPremium(),
                        u.getCreatedAt() == null ? "" : DISPLAY.format(u.getCreatedAt())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDetail getUserDetail(Long id) {
        User u = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        List<WorkoutSession> sessionList = sessionRepository.findByUserIdOrderByCompletedAtDesc(id);

        long totalSessions = sessionList.size();
        long totalSeconds = sessionList.stream().mapToLong(WorkoutSession::getTotalSeconds).sum();
        long totalExercises = sessionList.stream().mapToLong(WorkoutSession::getExercisesDone).sum();

        Double bmi = null;
        if (u.getHeightCm() != null && u.getHeightCm() > 0 && u.getWeightKg() != null && u.getWeightKg() > 0) {
            double hM = u.getHeightCm() / 100.0;
            bmi = Math.round((u.getWeightKg() / (hM * hM)) * 10.0) / 10.0;
        }

        Map<Long, String> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        List<SessionRow> recent = sessionList.stream().limit(20).map(s -> new SessionRow(
                s.getId(),
                s.getCategoryId() != null ? categoryMap.getOrDefault(s.getCategoryId(), "Bài tập tự chọn") : "Bài tập tự chọn",
                s.getTotalSeconds(),
                s.getExercisesDone(),
                s.getCompletedAt() == null ? "" : DISPLAY.format(s.getCompletedAt())
        )).toList();

        return new UserDetail(
                u.getId(), u.getEmail(), u.getDisplayName(), u.isPremium(),
                u.getHeightCm(), u.getWeightKg(), bmi,
                totalSessions, totalSeconds / 60, totalExercises,
                u.getCreatedAt() == null ? "" : DISPLAY.format(u.getCreatedAt()),
                recent
        );
    }

    @Transactional
    public void togglePremium(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setPremium(!user.isPremium());
        users.save(user);
    }
}
