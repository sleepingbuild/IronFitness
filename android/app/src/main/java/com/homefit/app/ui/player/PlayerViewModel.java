package com.homefit.app.ui.player;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.repository.ExerciseRepository;
import com.homefit.app.data.repository.WorkoutLogRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerViewModel extends AndroidViewModel {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final MutableLiveData<PlayerUiState> _uiState = new MutableLiveData<>(PlayerUiState.initial());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private List<ExerciseEntity> exercises = new ArrayList<>();
    private int currentIndex = 0;
    private PlayerState currentState = PlayerState.IDLE;
    private int remainingSeconds = 0;
    private int totalWorkoutSeconds = 0;
    
    private long currentCategoryId = -1;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        // FIXME: Thay bÃ¡ÂºÂ±ng DI hoÃ¡ÂºÂ·c Service Locator Ã„â€˜ÃƒÂºng chuÃ¡ÂºÂ©n cÃ¡Â»Â§a dÃ¡Â»Â± ÃƒÂ¡n. 
        // HiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i khÃ¡Â»Å¸i tÃ¡ÂºÂ¡o cÃ¡Â»Â©ng Ã„â€˜Ã¡Â»Æ’ code chÃ¡ÂºÂ¡y. SÃ¡ÂºÂ½ sÃ¡Â»Â­a lÃ¡ÂºÂ¡i sau nÃ¡ÂºÂ¿u dÃ¡Â»Â± ÃƒÂ¡n cÃƒÂ³ Dependency Injection.
        exerciseRepository = new ExerciseRepository(application);
        workoutLogRepository = new WorkoutLogRepository(application);
    }

    public LiveData<PlayerUiState> getUiState() {
        return _uiState;
    }

    public void loadExercises(long categoryId) {
        if (this.currentCategoryId == categoryId && !exercises.isEmpty()) {
            return; // Ã„ÂÃƒÂ£ load
        }
        this.currentCategoryId = categoryId;
        // Theo Ã„â€˜ÃƒÂºng kiÃ¡ÂºÂ¿n trÃƒÂºc, nÃƒÂªn trÃ¡ÂºÂ£ vÃ¡Â»Â LiveData, nhÃ†Â°ng Ã„â€˜Ã¡Â»Æ’ dÃ¡Â»â€¦ viÃ¡ÂºÂ¿t timer, 
        // ta sÃ¡ÂºÂ½ observe mÃ¡Â»â„¢t lÃ¡ÂºÂ§n rÃ¡Â»â€œi lÃ†Â°u vÃƒÂ o list local.
        exerciseRepository.getExercises(categoryId).observeForever(exercisesList -> {
            if (exercisesList != null && exercises.isEmpty()) {
                this.exercises = exercisesList;
                startWorkout();
            }
        });
    }

    private void startWorkout() {
        if (exercises.isEmpty()) return;
        currentIndex = 0;
        totalWorkoutSeconds = 0;
        startPreparing();
    }

    private void startPreparing() {
        currentState = PlayerState.PREPARING;
        remainingSeconds = 5; // 5s chuÃ¡ÂºÂ©n bÃ¡Â»â€¹
        updateUi();
        startTimer();
    }

    private void startResting() {
        ExerciseEntity prevExercise = exercises.get(currentIndex);
        currentState = PlayerState.RESTING;
        remainingSeconds = prevExercise.restSec > 0 ? prevExercise.restSec : 15;
        currentIndex++;
        updateUi();
        startTimer();
    }

    private void startExercising() {
        currentState = PlayerState.EXERCISING;
        ExerciseEntity currentExercise = exercises.get(currentIndex);
        if (currentExercise.durationSec != null && currentExercise.durationSec > 0) {
            remainingSeconds = currentExercise.durationSec;
            startTimer();
        } else {
            // Reps based, no timer for remaining
            remainingSeconds = 0;
            stopTimer(); // NgÃ†Â°ng timer Ã„â€˜Ã¡ÂºÂ¿m ngÃ†Â°Ã¡Â»Â£c, nhÃ†Â°ng cÃ¡ÂºÂ§n timer tÃ¡Â»â€¢ng (cÃ¡ÂºÂ£i tiÃ¡ÂºÂ¿n sau)
            // TÃ¡ÂºÂ¡m thÃ¡Â»Âi Ã„â€˜Ã¡ÂºÂ¿m tÃ¡Â»â€¢ng bÃ¡ÂºÂ±ng tay hoÃ¡ÂºÂ·c startTimer riÃƒÂªng cho tÃ¡Â»â€¢ng.
            startTotalTimerOnly();
        }
        updateUi();
    }

    public void onPlayPauseClicked() {
        if (currentState == PlayerState.PAUSED) {
            // Resume
            if (exercises.get(currentIndex).durationSec != null && exercises.get(currentIndex).durationSec > 0) {
                currentState = PlayerState.EXERCISING;
                startTimer();
            } else {
                // Ã„Âang trong bÃƒÂ i reps hoÃ¡ÂºÂ·c nghÃ¡Â»â€°, hoÃ¡ÂºÂ·c prepare
                if (remainingSeconds > 0) { // Ã„â€˜ang nghÃ¡Â»â€° hoÃ¡ÂºÂ·c chuÃ¡ÂºÂ©n bÃ¡Â»â€¹
                    currentState = currentIndex == 0 ? PlayerState.PREPARING : PlayerState.RESTING;
                    startTimer();
                } else {
                    currentState = PlayerState.EXERCISING;
                    startTotalTimerOnly();
                }
            }
        } else {
            // Pause
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
            startPreparing(); // hoÃ¡ÂºÂ·c vÃƒÂ o luÃƒÂ´n startExercising
        }
    }

    public void onRepsDoneClicked() {
        onNextClicked();
    }

    private void finishWorkout() {
        currentState = PlayerState.FINISHED;
        stopTimer();
        updateUi();
        
        // LÃ†Â°u vÃƒÂ o Room
        String clientUuid = UUID.randomUUID().toString();
        workoutLogRepository.saveWorkout(currentCategoryId, System.currentTimeMillis() - (totalWorkoutSeconds * 1000L), System.currentTimeMillis(), totalWorkoutSeconds, exercises.size());
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
        ExerciseEntity current = null;
        ExerciseEntity next = null;
        boolean isRepsBased = false;

        if (currentIndex >= 0 && currentIndex < exercises.size()) {
            current = exercises.get(currentIndex);
            isRepsBased = (current.durationSec == null || current.durationSec == 0) && (current.reps != null && current.reps > 0);
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


