package com.homefit.server.repository;

import com.homefit.server.domain.WorkoutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutItemRepository extends JpaRepository<WorkoutItem, Long> {
    long countByWorkoutId(Long workoutId);
    List<WorkoutItem> findByWorkoutIdOrderByPositionAsc(Long workoutId);
    void deleteByWorkoutId(Long workoutId);
}
