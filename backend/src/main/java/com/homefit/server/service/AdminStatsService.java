package com.homefit.server.service;

import com.homefit.server.domain.Role;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.UserRepository;
import com.homefit.server.repository.WorkoutSessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Số liệu tổng quan cho Dashboard quản trị. */
@Service
public class AdminStatsService {

    public record Stats(long users, long sessions, long categories, long exercises) {
    }

    private final UserRepository users;
    private final WorkoutSessionRepository sessions;
    private final CategoryRepository categories;
    private final ExerciseRepository exercises;

    public AdminStatsService(UserRepository users, WorkoutSessionRepository sessions,
                             CategoryRepository categories, ExerciseRepository exercises) {
        this.users = users;
        this.sessions = sessions;
        this.categories = categories;
        this.exercises = exercises;
    }

    @Transactional(readOnly = true)
    public Stats stats() {
        return new Stats(users.count(), sessions.count(),
                categories.countByActiveTrue(), exercises.countByActiveTrue());
    }

    @Transactional(readOnly = true)
    public long countPremiumUsers() {
        return users.countByRoleAndPremiumTrue(Role.USER);
    }
}
