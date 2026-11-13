package com.homefit.server.repository;

import com.homefit.server.domain.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByTypeOrderBySortOrderAsc(String type);
    List<Workout> findByLevelOrderBySortOrderAsc(String level);
    List<Workout> findAllByOrderBySortOrderAsc();
    java.util.Optional<Workout> findFirstByTitleVi(String titleVi);
}
