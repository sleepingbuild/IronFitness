package com.homefit.server.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Bảng workout_sessions: buổi tập đã hoàn thành. UNIQUE (user_id, client_uuid) chống trùng khi đồng bộ lại. */
@Entity
@Table(name = "workout_sessions")
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "client_uuid", nullable = false, length = 36)
    private String clientUuid;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "total_seconds", nullable = false)
    private int totalSeconds;

    @Column(name = "exercises_done", nullable = false)
    private int exercisesDone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getClientUuid() { return clientUuid; }
    public void setClientUuid(String clientUuid) { this.clientUuid = clientUuid; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public int getTotalSeconds() { return totalSeconds; }
    public void setTotalSeconds(int totalSeconds) { this.totalSeconds = totalSeconds; }

    public int getExercisesDone() { return exercisesDone; }
    public void setExercisesDone(int exercisesDone) { this.exercisesDone = exercisesDone; }

    public Instant getCreatedAt() { return createdAt; }
}
