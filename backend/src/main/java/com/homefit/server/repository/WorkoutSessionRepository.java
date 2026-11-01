package com.homefit.server.repository;

import com.homefit.server.domain.WorkoutSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    Optional<WorkoutSession> findByUserIdAndClientUuid(Long userId, String clientUuid);

    /** Lịch sử của một người dùng trong khoảng thời gian hoàn thành, mới nhất trước. */
    List<WorkoutSession> findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(
            Long userId, Instant from, Instant to);
}
