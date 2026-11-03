package com.homefit.server.repository;

import com.homefit.server.domain.WorkoutSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    Optional<WorkoutSession> findByUserIdAndClientUuid(Long userId, String clientUuid);

    /** Lịch sử của một người dùng trong khoảng thời gian hoàn thành, mới nhất trước. */
    List<WorkoutSession> findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(
            Long userId, Instant from, Instant to);

    @Query("SELECT COUNT(s) FROM WorkoutSession s WHERE s.userId = :userId")
    int countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(s.totalSeconds), 0) FROM WorkoutSession s WHERE s.userId = :userId")
    long sumDurationByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM WorkoutSession s WHERE s.userId = :userId AND s.completedAt >= :since")
    int countByUserIdSince(@Param("userId") Long userId, @Param("since") Instant since);
    
    @Query("SELECT DISTINCT CAST(s.completedAt AS java.time.LocalDate) FROM WorkoutSession s WHERE s.userId = :userId ORDER BY CAST(s.completedAt AS java.time.LocalDate) DESC")
    List<java.time.LocalDate> findDistinctWorkoutDaysDesc(@Param("userId") Long userId);
}
