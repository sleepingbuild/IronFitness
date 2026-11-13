package com.homefit.server.api;

import com.homefit.server.api.dto.ChallengeProgramDto;
import com.homefit.server.api.dto.ChallengeSessionDto;
import com.homefit.server.api.dto.WorkoutDto;
import com.homefit.server.api.dto.WorkoutItemDto;
import com.homefit.server.domain.ChallengeProgram;
import com.homefit.server.domain.ChallengeSession;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.repository.ChallengeProgramRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeApiController {

    private final ChallengeProgramRepository challengeProgramRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    public ChallengeApiController(ChallengeProgramRepository challengeProgramRepository,
                                  WorkoutRepository workoutRepository,
                                  ExerciseRepository exerciseRepository) {
        this.challengeProgramRepository = challengeProgramRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ChallengeProgramDto> getPrograms() {
        Map<Long, Workout> workoutMap = loadWorkoutMap();
        Map<Long, Exercise> exerciseMap = loadExerciseMap();

        List<ChallengeProgramDto> result = new ArrayList<>();
        for (String level : List.of("easy", "medium", "hard")) {
            challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(level, "PUBLISHED")
                    .ifPresent(program -> result.add(toDto(program, workoutMap, exerciseMap)));
        }
        return result;
    }

    @GetMapping("/{level}")
    @Transactional(readOnly = true)
    public ChallengeProgramDto getProgramByLevel(@PathVariable String level) {
        String lvl = level != null ? level.trim().toLowerCase(Locale.ROOT) : "easy";
        ChallengeProgram program = challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(lvl, "PUBLISHED")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy thử thách cấp độ: " + level));

        Map<Long, Workout> workoutMap = loadWorkoutMap();
        Map<Long, Exercise> exerciseMap = loadExerciseMap();
        return toDto(program, workoutMap, exerciseMap);
    }

    private Map<Long, Workout> loadWorkoutMap() {
        Map<Long, Workout> map = new HashMap<>();
        for (Workout w : workoutRepository.findAll()) {
            map.put(w.getId(), w);
        }
        return map;
    }

    private Map<Long, Exercise> loadExerciseMap() {
        Map<Long, Exercise> map = new HashMap<>();
        for (Exercise e : exerciseRepository.findAll()) {
            map.put(e.getId(), e);
        }
        return map;
    }

    private ChallengeProgramDto toDto(ChallengeProgram p, Map<Long, Workout> workoutMap, Map<Long, Exercise> exerciseMap) {
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
}
