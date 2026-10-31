package com.homefit.server.repository;

import com.homefit.server.domain.Exercise;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByActiveTrueOrderBySortOrderAscIdAsc();

    List<Exercise> findByCategoryIdAndActiveTrueOrderBySortOrderAscIdAsc(Long categoryId);

    List<Exercise> findByUpdatedAtAfterOrderBySortOrderAscIdAsc(Instant updatedAfter);

    List<Exercise> findByCategoryIdAndUpdatedAtAfterOrderBySortOrderAscIdAsc(Long categoryId, Instant updatedAfter);
}
