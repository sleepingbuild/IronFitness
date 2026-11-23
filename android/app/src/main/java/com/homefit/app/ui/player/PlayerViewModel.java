package com.homefit.app.ui.player;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.AssetExerciseRepository;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.data.local.model.CustomWorkout;
import com.homefit.app.data.repository.CustomWorkoutRepository;
import com.homefit.app.util.AppExecutors;

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

    private int currentExerciseActiveSeconds = 0;
    private int totalActiveDurationSec = 0;
    private double totalCaloriesBurned = 0.0;
    private double cachedWeightKg = 65.0;

    private String currentGroupKey = null;
    private String currentDifficulty = null;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        exerciseRepository = new AssetExerciseRepository(application);
        loadUserWeight();
    }

    private void loadUserWeight() {
        AppExecutors.diskIO().execute(() -> {
            var user = AppDatabase.getInstance(getApplication()).userProfileDao().getNow();
            if (user != null && user.weightKg != null && user.weightKg > 0) {
                cachedWeightKg = user.weightKg;
            }
        });
    }

    public LiveData<PlayerUiState> getUiState() {
        return _uiState;
    }

    public void loadCustomWorkout(String customWorkoutId, int resumeIndex) {
        if (customWorkoutId == null) return;
        List<LocalWorkoutDto> activeList = ActiveWorkoutHolder.getInstance().getAndClearActiveWorkouts();
        if (activeList != null && !activeList.isEmpty()) {
            this.currentGroupKey = "custom";
            this.currentDifficulty = customWorkoutId;
            this.exercises = activeList;
            startWorkout(resumeIndex);
            return;
        }
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
        if (groupKey == null || difficulty == null) return;
        List<LocalWorkoutDto> activeList = ActiveWorkoutHolder.getInstance().getAndClearActiveWorkouts();
        if (activeList != null && !activeList.isEmpty()) {
            this.currentGroupKey = groupKey;
            this.currentDifficulty = difficulty;
            this.exercises = activeList;
            startWorkout(resumeIndex);
            return;
        }

        if (groupKey.equals(currentGroupKey) && difficulty.equals(currentDifficulty) && exercises != null && !exercises.isEmpty()) {
            return; 
        }
        this.currentGroupKey = groupKey;
        this.currentDifficulty = difficulty;

        AppExecutors.diskIO().execute(() -> {
            // Ưu tiên đọc từ Room DB (đã đồng bộ từ Web Admin)
            List<LocalWorkoutDto> roomSyncedItems = tryLoadWorkoutItemsFromRoom(groupKey, difficulty, groupName);
            if (roomSyncedItems != null && !roomSyncedItems.isEmpty()) {
                AppExecutors.mainThread().execute(() -> {
                    this.exercises = roomSyncedItems;
                    startWorkout(resumeIndex);
                });
                return;
            }

            List<LocalExerciseGroupDto> all = exerciseRepository.getAllGroups();
            LocalExerciseGroupDto targetGroup = null;
            for (LocalExerciseGroupDto g : all) {
                if (g.group.equalsIgnoreCase(groupKey) && g.difficulty.equalsIgnoreCase(difficulty)) {
                    targetGroup = g;
                    break;
                }
            }

            // Handle 28-day challenge dynamically if targetGroup is null
            if (targetGroup == null && groupKey.startsWith("challenge_")) {
                String level = groupKey.replace("challenge_", "");
                try {
                    int dayIndex = Integer.parseInt(difficulty);
                    com.homefit.app.data.repository.ChallengeRepository challengeRepo = new com.homefit.app.data.repository.ChallengeRepository(getApplication());
                    targetGroup = challengeRepo.getSessionForDay(level, dayIndex);
                } catch (Exception ignored) {}
            }

            // Handle category workouts if groupKey is category_ID
            if (targetGroup == null && groupKey.startsWith("category_")) {
                try {
                    long categoryId = Long.parseLong(groupKey.replace("category_", ""));
                    loadCategoryExercisesFromDb(categoryId, resumeIndex);
                    return;
                } catch (Exception ignored) {}
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

            if (targetGroup != null && targetGroup.workouts != null && !targetGroup.workouts.isEmpty()) {
                final List<LocalWorkoutDto> loadedList = targetGroup.workouts;
                AppExecutors.mainThread().execute(() -> {
                    this.exercises = loadedList;
                    startWorkout(resumeIndex);
                });
            }
        });
    }

    private List<LocalWorkoutDto> tryLoadWorkoutItemsFromRoom(String groupKey, String difficulty, String groupName) {
        AppDatabase db = AppDatabase.getInstance(getApplication());
        List<com.homefit.app.data.local.entity.WorkoutEntity> allWorkouts = db.workoutDao().getAllNow();
        if (allWorkouts == null || allWorkouts.isEmpty()) return null;

        com.homefit.app.data.local.entity.WorkoutEntity matched = null;
        String g = groupKey != null ? groupKey.toLowerCase(Locale.US) : "";
        String d = difficulty != null ? difficulty.toLowerCase(Locale.US) : "";
        String n = groupName != null ? groupName.toLowerCase(Locale.US) : "";

        for (var w : allWorkouts) {
            String wTitle = w.titleVi != null ? w.titleVi.toLowerCase(Locale.US) : "";
            String wGoal = w.goal != null ? w.goal.toLowerCase(Locale.US) : "";
            String wLevel = w.level != null ? w.level.toLowerCase(Locale.US) : "";

            if (g.startsWith("challenge_")) {
                String lvlVi = g.contains("easy") ? "dễ" : (g.contains("hard") ? "khó" : "trung bình");
                if (wTitle.contains("ngày " + d) && (wTitle.contains(lvlVi) || wLevel.equalsIgnoreCase(g.replace("challenge_", "")))) {
                    matched = w;
                    break;
                }
            } else if (!n.isEmpty() && wTitle.contains(n)) {
                matched = w;
                break;
            } else if (!wGoal.isEmpty() && wGoal.equalsIgnoreCase(g) && (wLevel.equalsIgnoreCase(d) || wLevel.isEmpty())) {
                matched = w;
                break;
            }
        }

        if (matched != null) {
            List<com.homefit.app.data.local.entity.WorkoutItemEntity> items = db.workoutItemDao().getByWorkoutIdNow(matched.id);
            if (items != null && !items.isEmpty()) {
                List<LocalWorkoutDto> dtos = new ArrayList<>();
                for (var item : items) {
                    LocalWorkoutDto dto = new LocalWorkoutDto();
                    dto.id = String.valueOf(item.exerciseId);
                    dto.name = item.exerciseName;
                    dto.mediaUrl = item.exerciseMediaUrl;
                    dto.reps_sets = "time".equalsIgnoreCase(item.measure) ? item.value + "s" : item.value + " lần";
                    dto.target = "Nghỉ " + item.restAfterSec + "s";
                    dto.instructions = new ArrayList<>();
                    dto.instructions.add("Thực hiện " + item.exerciseName + " theo đúng kỹ thuật.");
                    dto.tips = "Giữ nhịp thở đều.";
                    dtos.add(dto);
                }
                return dtos;
            }
        }
        return null;
    }

    private void loadCategoryExercisesFromDb(long categoryId, int resumeIndex) {
        AppExecutors.diskIO().execute(() -> {
            var dbList = AppDatabase.getInstance(getApplication()).exerciseDao().getByCategoryNow(categoryId);
            if (dbList != null && !dbList.isEmpty()) {
                List<LocalWorkoutDto> workouts = new ArrayList<>();
                for (var e : dbList) {
                    LocalWorkoutDto dto = new LocalWorkoutDto();
                    dto.id = String.valueOf(e.id);
                    dto.name = e.name;
                    dto.mediaUrl = e.mediaUrl;
                    dto.target = e.description != null ? e.description : "";
                    dto.reps_sets = e.durationSec != null ? e.durationSec + "s" : (e.reps != null ? e.reps + " lần" : "12 lần");
                    dto.instructions = new ArrayList<>();
                    if (e.description != null) dto.instructions.add(e.description);
                    dto.tips = "Giữ tư thế chuẩn.";
                    workouts.add(dto);
                }
                AppExecutors.mainThread().execute(() -> {
                    this.exercises = workouts;
                    startWorkout(resumeIndex);
                });
            }
        });
    }

    public void startWorkout(int resumeIndex) {
        if (exercises.isEmpty()) return;
        if (resumeIndex >= 0 && resumeIndex < exercises.size()) {
            currentIndex = resumeIndex;
        } else {
            currentIndex = 0;
        }
        totalWorkoutSeconds = 0;
        totalActiveDurationSec = 0;
        totalCaloriesBurned = 0.0;
        currentExerciseActiveSeconds = 0;
        com.homefit.app.util.AnalyticsTracker.logWorkoutStart(getApplication(), currentGroupKey, currentGroupKey, currentDifficulty);
        enrichExercisesFromDb();
        startPreparing();
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private void enrichExercisesFromDb() {
        AppExecutors.diskIO().execute(() -> {
            var dbList = AppDatabase.getInstance(getApplication()).exerciseDao().getAllNow();
            if (dbList == null || dbList.isEmpty()) return;
            boolean changed = false;
            for (LocalWorkoutDto dto : exercises) {
                if (dto.name == null) continue;
                String normDto = stripAccents(dto.name).toLowerCase(Locale.US).replaceAll("[^a-zA-Z0-9]", "");
                for (var e : dbList) {
                    if (e.name == null) continue;
                    String normE = stripAccents(e.name).toLowerCase(Locale.US).replaceAll("[^a-zA-Z0-9]", "");
                    if (e.name.equalsIgnoreCase(dto.name)
                            || (!normE.isEmpty() && normDto.contains(normE))
                            || (!normDto.isEmpty() && normE.contains(normDto))) {
                        if (e.mediaUrl != null && !e.mediaUrl.isBlank()) {
                            dto.mediaUrl = e.mediaUrl;
                            changed = true;
                        }
                        if (e.description != null && !e.description.isBlank()) {
                            dto.target = e.description;
                            if (dto.instructions == null || dto.instructions.isEmpty()) {
                                dto.instructions = new ArrayList<>();
                                dto.instructions.add(e.description);
                                changed = true;
                            }
                        }
                        break;
                    }
                }
            }
            if (changed) {
                AppExecutors.mainThread().execute(this::updateUi);
            }
        });
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
        calculateAndAccumulateExerciseStats();
        currentState = PlayerState.RESTING;
        remainingSeconds = 25; // 25s rest
        currentIndex++;
        updateUi();
        startTimer();
    }

    private void calculateAndAccumulateExerciseStats() {
        if (currentIndex >= 0 && currentIndex < exercises.size()) {
            LocalWorkoutDto exercise = exercises.get(currentIndex);
            int duration = parseDurationInSeconds(exercise.reps_sets);
            int activeSec = currentExerciseActiveSeconds;
            if (duration > 0) {
                if (activeSec <= 0) activeSec = duration;
            } else {
                int reps = parseReps(exercise.reps_sets);
                int minRepsSec = Math.max(1, (int) Math.ceil(reps * 2.5));
                activeSec = Math.max(activeSec, minRepsSec);
            }
            totalActiveDurationSec += activeSec;
            double met = getMetForExercise(exercise.name, exercise.target);
            double kcal = (met * 3.5 * cachedWeightKg * (activeSec / 60.0)) / 200.0;
            totalCaloriesBurned += kcal;
        }
        currentExerciseActiveSeconds = 0;
    }

    private double getMetForExercise(String name, String target) {
        String combined = ((name != null ? name : "") + " " + (target != null ? target : "")).toLowerCase(Locale.US);
        if (combined.contains("burpee") || combined.contains("jumping jack") || combined.contains("mountain climber")
                || combined.contains("leo núi") || combined.contains("jump squat") || combined.contains("nhảy")
                || combined.contains("chạy nâng cao đùi") || combined.contains("đấm bốc")) {
            return 8.5;
        }
        if (combined.contains("hít đất") || combined.contains("chống đẩy") || combined.contains("push-up") || combined.contains("push up")
                || combined.contains("dips") || combined.contains("nhún tay") || combined.contains("pull-up") || combined.contains("pull up")
                || combined.contains("hít xà") || combined.contains("shoulder tap")) {
            return 6.5;
        }
        if (combined.contains("squat") || combined.contains("lunge") || combined.contains("chùng chân")
                || combined.contains("wall sit") || combined.contains("calf raise") || combined.contains("nâng gót")
                || combined.contains("glute bridge") || combined.contains("bước lên bục")) {
            return 5.5;
        }
        if (combined.contains("plank") || combined.contains("crunch") || combined.contains("gập bụng")
                || combined.contains("leg raise") || combined.contains("nâng chân") || combined.contains("v-up")
                || combined.contains("dead bug") || combined.contains("hollow hold") || combined.contains("đạp xe")) {
            return 4.0;
        }
        if (combined.contains("stretch") || combined.contains("giãn cơ") || combined.contains("pose")
                || combined.contains("tư thế") || combined.contains("circles") || combined.contains("xoay")
                || combined.contains("cat-cow") || combined.contains("child") || combined.contains("cobra")) {
            return 2.5;
        }
        return 4.0;
    }

    private float getAdaptiveMultiplier() {
        return new com.homefit.app.ui.settings.SettingsStore(getApplication()).getDifficultyMultiplier();
    }

    private int parseReps(String repsSets) {
        if (repsSets == null || repsSets.isEmpty()) return 10;
        int baseReps = 10;
        Matcher m = Pattern.compile("(\\d+)").matcher(repsSets);
        if (m.find()) {
            try {
                String val = m.group(1);
                if (val != null) {
                    baseReps = Integer.parseInt(val);
                }
            } catch (Exception ignored) {}
        }
        float multiplier = getAdaptiveMultiplier();
        return Math.max(1, Math.round(baseReps * multiplier));
    }

    private int parseDurationInSeconds(String repsSets) {
        if (repsSets == null || repsSets.isEmpty()) return 0;

        String text = repsSets.toLowerCase(Locale.US);
        int baseSec = 0;

        Matcher mSec = Pattern.compile("(\\d+)\\s*(s(?!ets)|giây|giay|sec)").matcher(text);
        if (mSec.find()) {
            try {
                String matchGroup = mSec.group(1);
                if (matchGroup != null) {
                    baseSec = Integer.parseInt(matchGroup);
                }
            } catch (Exception ignored) {}
        }

        if (baseSec == 0) {
            Matcher mMmSs = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(text);
            if (mMmSs.find()) {
                try {
                    String minGroup = mMmSs.group(1);
                    String secGroup = mMmSs.group(2);
                    if (minGroup != null && secGroup != null) {
                        int mins = Integer.parseInt(minGroup);
                        int secs = Integer.parseInt(secGroup);
                        baseSec = mins * 60 + secs;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (baseSec > 0) {
            float multiplier = getAdaptiveMultiplier();
            return Math.max(5, Math.round(baseSec * multiplier));
        }

        return 0;
    }

    private void startExercising() {
        if (exercises == null || exercises.isEmpty() || currentIndex < 0 || currentIndex >= exercises.size()) return;
        currentState = PlayerState.EXERCISING;
        currentExerciseActiveSeconds = 0;
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
        if (exercises == null || exercises.isEmpty() || currentIndex < 0 || currentIndex >= exercises.size()) return;
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
        calculateAndAccumulateExerciseStats();
        currentState = PlayerState.FINISHED;
        stopTimer();
        com.homefit.app.util.AnalyticsTracker.logWorkoutComplete(getApplication(), currentGroupKey, totalActiveDurationSec, totalCaloriesBurned);
        updateUi();
    }

    private void startTimer() {
        stopTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                totalWorkoutSeconds++;
                if (currentState == PlayerState.EXERCISING) {
                    currentExerciseActiveSeconds++;
                }
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
                if (currentState == PlayerState.EXERCISING) {
                    currentExerciseActiveSeconds++;
                }
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
                isRepsBased,
                totalActiveDurationSec,
                totalCaloriesBurned
        ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTimer();
    }
}
