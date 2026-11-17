package com.homefit.app.ui.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;

public class PlayerUiState {
    public final PlayerState state;
    @Nullable public final LocalWorkoutDto currentExercise;
    @Nullable public final LocalWorkoutDto nextExercise;
    public final int currentExerciseIndex;
    public final int totalExercises;
    public final int remainingSeconds; // Remaining seconds for current exercise or rest
    public final int totalWorkoutSeconds; // Total elapsed time
    public final boolean isRepsBased;
    public final int activeDurationSec; // Active exercise time (excluding rest)
    public final double caloriesBurned; // MET-based calorie burn

    public PlayerUiState(
            @NonNull PlayerState state,
            @Nullable LocalWorkoutDto currentExercise,
            @Nullable LocalWorkoutDto nextExercise,
            int currentExerciseIndex,
            int totalExercises,
            int remainingSeconds,
            int totalWorkoutSeconds,
            boolean isRepsBased,
            int activeDurationSec,
            double caloriesBurned) {
        this.state = state;
        this.currentExercise = currentExercise;
        this.nextExercise = nextExercise;
        this.currentExerciseIndex = currentExerciseIndex;
        this.totalExercises = totalExercises;
        this.remainingSeconds = remainingSeconds;
        this.totalWorkoutSeconds = totalWorkoutSeconds;
        this.isRepsBased = isRepsBased;
        this.activeDurationSec = activeDurationSec;
        this.caloriesBurned = caloriesBurned;
    }

    public static PlayerUiState initial() {
        return new PlayerUiState(PlayerState.IDLE, null, null, 0, 0, 0, 0, false, 0, 0.0);
    }
}