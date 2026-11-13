package com.homefit.server.api;

import com.homefit.server.api.dto.ContentSyncResponse;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/content")
public class ContentSyncApiController {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final CategoryRepository categoryRepository;

    public ContentSyncApiController(ExerciseRepository exerciseRepository,
                                  WorkoutRepository workoutRepository,
                                  CategoryRepository categoryRepository) {
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/sync")
    @Transactional(readOnly = true)
    public ContentSyncResponse getSyncInfo() {
        List<Exercise> exercises = exerciseRepository.findAll();
        List<Workout> workouts = workoutRepository.findAll();

        Instant latest = Instant.EPOCH;
        for (Exercise e : exercises) {
            if (e.getUpdatedAt() != null && e.getUpdatedAt().isAfter(latest)) {
                latest = e.getUpdatedAt();
            }
        }
        for (Workout w : workouts) {
            if (w.getUpdatedAt() != null && w.getUpdatedAt().isAfter(latest)) {
                latest = w.getUpdatedAt();
            }
        }

        return new ContentSyncResponse(latest, exercises.size(), workouts.size(), categoryRepository.count());
    }
}
