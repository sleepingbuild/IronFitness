package com.homefit.server.api;

import com.homefit.server.api.dto.WorkoutDto;
import com.homefit.server.api.dto.WorkoutItemDto;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutApiController {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutApiController(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkouts(@RequestParam(required = false) String type,
                                         @RequestParam(required = false) String level,
                                         @RequestParam(required = false) String goal) {
        List<Workout> list = workoutRepository.findAllByOrderBySortOrderAsc();
        Map<Long, Exercise> exerciseMap = loadExerciseMap();

        List<WorkoutDto> result = new ArrayList<>();
        for (Workout w : list) {
            if ("PUBLISHED".equalsIgnoreCase(w.getStatus())) {
                if (type != null && !type.equalsIgnoreCase(w.getType())) continue;
                if (level != null && !level.equalsIgnoreCase(w.getLevel())) continue;
                if (goal != null && !goal.equalsIgnoreCase(w.getGoal())) continue;
                result.add(toDto(w, exerciseMap));
            }
        }
        return result;
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public WorkoutDto getWorkout(@PathVariable Long id) {
        Workout w = workoutRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy buổi tập #" + id));
        Map<Long, Exercise> exerciseMap = loadExerciseMap();
        return toDto(w, exerciseMap);
    }

    private Map<Long, Exercise> loadExerciseMap() {
        Map<Long, Exercise> map = new HashMap<>();
        for (Exercise e : exerciseRepository.findAll()) {
            map.put(e.getId(), e);
        }
        return map;
    }

    private WorkoutDto toDto(Workout w, Map<Long, Exercise> exerciseMap) {
        List<WorkoutItemDto> items = new ArrayList<>();
        if (w.getItems() != null) {
            for (var item : w.getItems()) {
                Exercise ex = exerciseMap.get(item.getExerciseId());
                String exName = ex != null ? ex.getName() : "Bài tập #" + item.getExerciseId();
                String exMediaUrl = ex != null ? ex.getMediaUrl() : "";
                String exMediaType = ex != null && ex.getMediaType() != null ? ex.getMediaType().name() : "GIF";
                items.add(WorkoutItemDto.from(item, exName, exMediaUrl, exMediaType));
            }
        }
        return WorkoutDto.from(w, items);
    }
}
