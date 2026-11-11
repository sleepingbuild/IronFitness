package com.homefit.app.ui.player;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AssetExerciseRepository;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.data.local.model.CustomWorkout;
import com.homefit.app.data.repository.CustomWorkoutRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerViewModel extends AndroidViewModel {

    private final AssetExerciseRepository exerciseRepository;
    private final MutableLiveData<PlayerUiState> _uiState = new MutableLiveData<>(PlayerUiState.initial());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private List<LocalWorkoutDto> exercises = new ArrayList<>();
    private int currentIndex = 0;
    private PlayerState currentState = PlayerState.IDLE;
    private int remainingSeconds = 0;
    private int totalWorkoutSeconds = 0;

    private String currentGroupKey = null;
    private String currentDifficulty = null;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        exerciseRepository = new AssetExerciseRepository(application);
    }

    public LiveData<PlayerUiState> getUiState() {
        return _uiState;
    }

    public void loadCustomWorkout(String customWorkoutId, int resumeIndex) {
        if (customWorkoutId == null) return;
        CustomWorkoutRepository customRepo = new CustomWorkoutRepository(getApplication());
        CustomWorkout customWorkout = customRepo.getWorkoutById(customWorkoutId);
        if (customWorkout != null && customWorkout.items != null && !customWorkout.items.isEmpty()) {
            this.currentGroupKey = "custom";
            this.currentDifficulty = customWorkoutId;
            this.exercises = customWorkout.toLocalWorkoutDtos();
            startWorkout(resumeIndex);
        }
    }

    public void loadExercises(String groupKey, String difficulty, String groupName, int resumeIndex) {
        if (groupKey.equals(currentGroupKey) && difficulty.equals(currentDifficulty) && !exercises.isEmpty()) {
            return; 
        }
        this.currentGroupKey = groupKey;
        this.currentDifficulty = difficulty;

        List<LocalExerciseGroupDto> all = exerciseRepository.getAllGroups();
        LocalExerciseGroupDto targetGroup = null;
        for (LocalExerciseGroupDto g : all) {
            if (g.group.equalsIgnoreCase(groupKey) && g.difficulty.equalsIgnoreCase(difficulty)) {
                targetGroup = g;
                break;
            }
        }

        // Handle generated goals dynamically
        if (targetGroup == null) {
            if ("muscle".equals(groupKey)) {
                if ("Chân & Mông".equals(groupName)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createMuscleMedium2();
                else if ("Thân trên + Lõi".equals(groupName)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createMuscleMedium3();
                else targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createMuscleMedium1();
            } else if ("fatloss".equals(groupKey)) {
                if ("Easy".equalsIgnoreCase(difficulty)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createFatLossEasy();
                else if ("Medium".equalsIgnoreCase(difficulty)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createFatLossMedium();
                else if ("Hard".equalsIgnoreCase(difficulty)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createFatLossHard();
            } else if ("keepfit".equals(groupKey)) {
                if ("Easy".equalsIgnoreCase(difficulty)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createKeepFitEasy();
                else if ("Medium".equalsIgnoreCase(difficulty)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createKeepFitMedium();
                else if ("Hard".equalsIgnoreCase(difficulty)) targetGroup = com.homefit.app.ui.workout.PopularWorkoutGenerator.createKeepFitHard();
            }
        }

        if (targetGroup != null && targetGroup.workouts != null) {
            this.exercises = targetGroup.workouts;
            startWorkout(resumeIndex);
        }
    }

    public void startWorkout(int resumeIndex) {
        if (exercises.isEmpty()) return;
        if (resumeIndex >= 0 && resumeIndex < exercises.size()) {
            currentIndex = resumeIndex;
        } else {
            currentIndex = 0;
        }
        totalWorkoutSeconds = 0;
        startPreparing();
    }

    public void addRestSeconds(int seconds) {
        if (currentState == PlayerState.RESTING) {
            remainingSeconds += seconds;
            updateUi();
        }
    }

    public void skipRest() {
        if (currentState == PlayerState.RESTING) {
            stopTimer();
            startExercising();
        }
    }

    public void pauseWorkout() {
        if (currentState != PlayerState.PAUSED && currentState != PlayerState.FINISHED) {
            currentState = PlayerState.PAUSED;
            stopTimer();
            updateUi();
        }
    }

    private void startPreparing() {
        currentState = PlayerState.PREPARING;
        remainingSeconds = 5; 
        updateUi();
        startTimer();
    }

    private void startResting() {
        currentState = PlayerState.RESTING;
        remainingSeconds = 25; // 25s rest
        currentIndex++;
        updateUi();
        startTimer();
    }

    private int parseDurationInSeconds(String repsSets) {
        if (repsSets == null || repsSets.isEmpty()) return 0;

        String text = repsSets.toLowerCase(Locale.US);

        Matcher mSec = Pattern.compile("(\\d+)\\s*(s(?!ets)|giây|giay|sec)").matcher(text);
        if (mSec.find()) {
            try {
                String matchGroup = mSec.group(1);
                if (matchGroup != null) {
                    return Integer.parseInt(matchGroup);
                }
            } catch (Exception ignored) {}
        }

        Matcher mMmSs = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(text);
        if (mMmSs.find()) {
            try {
                String minGroup = mMmSs.group(1);
                String secGroup = mMmSs.group(2);
                if (minGroup != null && secGroup != null) {
                    int mins = Integer.parseInt(minGroup);
                    int secs = Integer.parseInt(secGroup);
                    return mins * 60 + secs;
                }
            } catch (Exception ignored) {}
        }

        return 0;
    }

    private void startExercising() {
        currentState = PlayerState.EXERCISING;
        LocalWorkoutDto currentExercise = exercises.get(currentIndex);

        int duration = parseDurationInSeconds(currentExercise.reps_sets);

        if (duration > 0) {
            remainingSeconds = duration;
            startTimer();
        } else {
            remainingSeconds = 0;
            stopTimer(); 
            startTotalTimerOnly();
        }
        updateUi();
    }

    public void onPlayPauseClicked() {
        if (currentState == PlayerState.PAUSED) {
            LocalWorkoutDto currentExercise = exercises.get(currentIndex);
            int duration = parseDurationInSeconds(currentExercise.reps_sets);

            if (duration > 0) {
                currentState = PlayerState.EXERCISING;
                startTimer();
            } else {
                if (remainingSeconds > 0) { 
                    currentState = currentIndex == 0 ? PlayerState.PREPARING : PlayerState.RESTING;
                    startTimer();
                } else {
                    currentState = PlayerState.EXERCISING;
                    startTotalTimerOnly();
                }
            }
        } else {
            currentState = PlayerState.PAUSED;
            stopTimer();
        }
        updateUi();
    }

    public void onNextClicked() {
        stopTimer();
        if (currentIndex < exercises.size() - 1) {
            startResting();
        } else {
            finishWorkout();
        }
    }

    public void onPreviousClicked() {
        if (currentIndex > 0) {
            stopTimer();
            currentIndex--;
            startPreparing();
        }
    }

    public void onRepsDoneClicked() {
        onNextClicked();
    }

    private void finishWorkout() {
        currentState = PlayerState.FINISHED;
        stopTimer();
        updateUi();
    }

    private void startTimer() {
        stopTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                totalWorkoutSeconds++;
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    updateUi();
                    handler.postDelayed(this, 1000);
                } else {
                    handleTimerEnd();
                }
            }
        };
        handler.postDelayed(timerRunnable, 1000);
    }

    private void startTotalTimerOnly() {
        stopTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                totalWorkoutSeconds++;
                updateUi();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(timerRunnable, 1000);
    }

    private void handleTimerEnd() {
        if (currentState == PlayerState.PREPARING || currentState == PlayerState.RESTING) {
            startExercising();
        } else if (currentState == PlayerState.EXERCISING) {
            if (currentIndex < exercises.size() - 1) {
                startResting();
            } else {
                finishWorkout();
            }
        }
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    private void updateUi() {
        LocalWorkoutDto current = null;
        LocalWorkoutDto next = null;
        boolean isRepsBased = false;

        if (currentIndex >= 0 && currentIndex < exercises.size()) {
            current = exercises.get(currentIndex);
            int duration = parseDurationInSeconds(current.reps_sets);
            isRepsBased = (duration == 0);
            if (currentIndex + 1 < exercises.size()) {
                next = exercises.get(currentIndex + 1);
            }
        }

        _uiState.setValue(new PlayerUiState(
                currentState,
                current,
                next,
                currentIndex,
                exercises.size(),
                remainingSeconds,
                totalWorkoutSeconds,
                isRepsBased
        ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTimer();
    }
}
