package com.homefit.server.api;

import com.homefit.server.api.dto.ChallengeProgramDto;
import com.homefit.server.api.dto.ChallengeSessionDto;
import com.homefit.server.api.dto.ContentSyncResponse;
import com.homefit.server.api.dto.WorkoutDto;
import com.homefit.server.api.dto.WorkoutItemDto;
import com.homefit.server.domain.ChallengeProgram;
import com.homefit.server.domain.ChallengeSession;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ChallengeProgramRepository;
import com.homefit.server.repository.ContentReleaseRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/content")
public class ContentSyncApiController {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final CategoryRepository categoryRepository;
    private final ContentReleaseRepository contentReleaseRepository;
    private final ChallengeProgramRepository challengeProgramRepository;

    public ContentSyncApiController(ExerciseRepository exerciseRepository,
                                  WorkoutRepository workoutRepository,
                                  CategoryRepository categoryRepository,
                                  ContentReleaseRepository contentReleaseRepository,
                                  ChallengeProgramRepository challengeProgramRepository) {
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.categoryRepository = categoryRepository;
        this.contentReleaseRepository = contentReleaseRepository;
        this.challengeProgramRepository = challengeProgramRepository;
    }

    @GetMapping("/sync")
    @Transactional(readOnly = true)
    public ContentSyncResponse getSyncInfo() {
        List<Exercise> exercises = exerciseRepository.findAll();
        List<Workout> workouts = workoutRepository.findAll();

        Map<Long, Exercise> exerciseMap = new HashMap<>();
        for (Exercise e : exercises) {
            exerciseMap.put(e.getId(), e);
        }

        Map<Long, Workout> workoutMap = new HashMap<>();
        for (Workout w : workouts) {
            workoutMap.put(w.getId(), w);
        }

        Instant latest = Instant.EPOCH;
        long totalItems = 0;
        List<WorkoutDto> workoutDtos = new ArrayList<>();
        for (Exercise e : exercises) {
            if (e.getUpdatedAt() != null && e.getUpdatedAt().isAfter(latest)) {
                latest = e.getUpdatedAt();
            }
        }
        for (Workout w : workouts) {
            if (w.getUpdatedAt() != null && w.getUpdatedAt().isAfter(latest)) {
                latest = w.getUpdatedAt();
            }
            if (w.getItems() != null) {
                totalItems += w.getItems().size();
            }
            workoutDtos.add(toWorkoutDto(w, exerciseMap));
        }

        List<ChallengeProgramDto> challengeDtos = new ArrayList<>();
        for (String level : List.of("easy", "medium", "hard")) {
            challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(level, "PUBLISHED")
                    .ifPresent(program -> challengeDtos.add(toChallengeDto(program, workoutMap, exerciseMap)));
        }

        long releaseVersion = contentReleaseRepository.findTopByOrderByVersionDesc()
                .map(r -> (long) r.getVersion())
                .orElse(latest.getEpochSecond() > 0 ? latest.getEpochSecond() : 1L);

        return new ContentSyncResponse(
                latest,
                exercises.size(),
                workouts.size(),
                categoryRepository.count(),
                releaseVersion,
                totalItems,
                workoutDtos,
                challengeDtos
        );
    }

    private WorkoutDto toWorkoutDto(Workout w, Map<Long, Exercise> exerciseMap) {
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

    private ChallengeProgramDto toChallengeDto(ChallengeProgram p, Map<Long, Workout> workoutMap, Map<Long, Exercise> exerciseMap) {
        List<ChallengeSessionDto> sessionDtos = new ArrayList<>();
        if (p.getSessions() != null) {
            for (ChallengeSession s : p.getSessions()) {
                Workout w = s.getWorkoutId() != null ? workoutMap.get(s.getWorkoutId()) : null;
                WorkoutDto wDto = w != null ? toWorkoutDto(w, exerciseMap) : null;
                sessionDtos.add(ChallengeSessionDto.from(s, wDto));
            }
        }
        return ChallengeProgramDto.from(p, sessionDtos);
    }
}
