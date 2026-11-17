package com.homefit.app.ui.player;

import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder dùng chung để truyền trực tiếp danh sách bài tập (LocalWorkoutDto)
 * đã có sẵn từ UI/ViewModel sang PlayerViewModel mà không cần truy vấn lại.
 */
public class ActiveWorkoutHolder {

    private static volatile ActiveWorkoutHolder instance;
    private List<LocalWorkoutDto> activeWorkouts = null;

    private ActiveWorkoutHolder() {
    }

    public static ActiveWorkoutHolder getInstance() {
        if (instance == null) {
            synchronized (ActiveWorkoutHolder.class) {
                if (instance == null) {
                    instance = new ActiveWorkoutHolder();
                }
            }
        }
        return instance;
    }

    public void setActiveWorkouts(List<LocalWorkoutDto> workouts) {
        if (workouts != null && !workouts.isEmpty()) {
            this.activeWorkouts = new ArrayList<>(workouts);
        } else {
            this.activeWorkouts = null;
        }
    }

    public List<LocalWorkoutDto> getActiveWorkouts() {
        return activeWorkouts != null ? new ArrayList<>(activeWorkouts) : null;
    }

    public List<LocalWorkoutDto> getAndClearActiveWorkouts() {
        List<LocalWorkoutDto> result = activeWorkouts;
        activeWorkouts = null;
        return result;
    }

    public boolean hasActiveWorkouts() {
        return activeWorkouts != null && !activeWorkouts.isEmpty();
    }

    public void clear() {
        activeWorkouts = null;
    }
}
