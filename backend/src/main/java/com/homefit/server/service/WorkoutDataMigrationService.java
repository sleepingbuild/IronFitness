package com.homefit.server.service;

import com.homefit.server.domain.ChallengeProgram;
import com.homefit.server.domain.ChallengeSession;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.domain.WorkoutItem;
import com.homefit.server.repository.ChallengeProgramRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class WorkoutDataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(WorkoutDataMigrationService.class);

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final ChallengeProgramRepository challengeProgramRepository;

    public WorkoutDataMigrationService(WorkoutRepository workoutRepository,
                                       ExerciseRepository exerciseRepository,
                                       ChallengeProgramRepository challengeProgramRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.challengeProgramRepository = challengeProgramRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        log.info("Bắt đầu kiểm tra và bổ sung tự động bài tập con cho toàn bộ các buổi tập trống trên TiDB Cloud...");
        int count = populateAllEmptyWorkouts();
        log.info("Hoàn tất tự động bổ sung bài tập con! Đã nạp dữ liệu cho {} buổi tập.", count);
    }

    @Transactional
    public int populateAllEmptyWorkouts() {
        Map<String, Exercise> exerciseMap = loadExerciseMap();
        List<Workout> allWorkouts = workoutRepository.findAll();
        if (allWorkouts.isEmpty()) {
            return 0;
        }

        Map<Long, ChallengeSession> challengeSessionMap = new HashMap<>();
        List<ChallengeProgram> programs = challengeProgramRepository.findAll();
        for (ChallengeProgram p : programs) {
            if (p.getSessions() != null) {
                for (ChallengeSession s : p.getSessions()) {
                    if (s.getWorkoutId() != null) {
                        challengeSessionMap.put(s.getWorkoutId(), s);
                    }
                }
            }
        }

        int updated = 0;
        for (Workout w : allWorkouts) {
            if (w.getItems() != null && !w.getItems().isEmpty()) {
                continue;
            }

            List<Exercise> exList = resolveExercisesForWorkout(w, challengeSessionMap.get(w.getId()), exerciseMap);
            if (exList != null && !exList.isEmpty()) {
                populateItemsForWorkout(w, exList);
                workoutRepository.save(w);
                updated++;
            }
        }
        return updated;
    }

    private void populateItemsForWorkout(Workout w, List<Exercise> exList) {
        List<WorkoutItem> items = w.getItems();
        if (items == null) {
            items = new ArrayList<>();
            w.setItems(items);
        } else {
            items.clear();
        }

        int restSec = w.getRestSec() > 0 ? w.getRestSec() : 15;
        int pos = 1;
        for (Exercise e : exList) {
            if (e == null) continue;
            WorkoutItem item = new WorkoutItem();
            item.setWorkout(w);
            item.setPosition(pos++);
            item.setExerciseId(e.getId());
            item.setBlock(pos <= 3 ? "A" : (pos > exList.size() - 2 ? "C" : "B"));
            item.setRoundNum(1);
            if (e.getDurationSec() != null && e.getDurationSec() > 0) {
                item.setMeasure("time");
                item.setValue(e.getDurationSec());
            } else {
                item.setMeasure("reps");
                item.setValue(e.getReps() != null ? e.getReps() : 12);
            }
            item.setRestAfterSec(restSec);
            items.add(item);
        }
    }

    private List<Exercise> resolveExercisesForWorkout(Workout w, ChallengeSession cs, Map<String, Exercise> exerciseMap) {
        String title = stripAccents(w.getTitleVi()).toLowerCase(Locale.US);
        String goal = stripAccents(w.getGoal() != null ? w.getGoal() : "").toLowerCase(Locale.US);
        String muscle = stripAccents(w.getPrimaryMuscle() != null ? w.getPrimaryMuscle() : "").toLowerCase(Locale.US);
        String level = stripAccents(w.getLevel() != null ? w.getLevel() : "").toLowerCase(Locale.US);
        String type = stripAccents(w.getType() != null ? w.getType() : "").toLowerCase(Locale.US);

        if (cs != null || type.contains("challenge") || title.contains("thu thach") || title.contains("ngay ")) {
            int dayIndex = cs != null ? cs.getDayIndex() : extractDayIndex(title);
            if (dayIndex > 0) {
                String dayType = getDayTypeName(dayIndex);
                boolean isCycle2 = ((dayIndex - 1) % 7 + 1) > 3;
                return buildChallengeExercises(dayType, isCycle2, exerciseMap);
            }
        }

        if (type.contains("tabata") || goal.contains("tabata") || title.contains("tabata")) {
            return listOfEx(
                    getEx(exerciseMap, "Nhảy dang tay chân (Jumping Jacks)"),
                    getEx(exerciseMap, "Squat bật nhảy (Jump Squat)"),
                    getEx(exerciseMap, "Leo núi tại chỗ (Mountain Climber)"),
                    getEx(exerciseMap, "Burpees")
            );
        }

        if (type.contains("stretch") || goal.contains("stretch") || title.contains("keo gian")) {
            return listOfEx(
                    getEx(exerciseMap, "Cat-Cow"),
                    getEx(exerciseMap, "Child's Pose"),
                    getEx(exerciseMap, "Chest Opener"),
                    getEx(exerciseMap, "Standing Hamstring Stretch")
            );
        }

        if (goal.contains("fatloss") || title.contains("dot mo") || title.contains("hiit") || title.contains("dot chay")) {
            if (level.contains("hard") || title.contains("cao") || title.contains("tang toc")) {
                return listOfEx(
                        getEx(exerciseMap, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exerciseMap, "Squat bật nhảy (Jump Squat)"),
                        getEx(exerciseMap, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exerciseMap, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exerciseMap, "Burpees"),
                        getEx(exerciseMap, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exerciseMap, "Standing Quad Stretch"),
                        getEx(exerciseMap, "Child's Pose")
                );
            } else if (level.contains("medium") || title.contains("vua")) {
                return listOfEx(
                        getEx(exerciseMap, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exerciseMap, "Arm Circles"),
                        getEx(exerciseMap, "Squat bật nhảy (Jump Squat)"),
                        getEx(exerciseMap, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exerciseMap, "Chùng chân bước lùi (Reverse Lunge)"),
                        getEx(exerciseMap, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exerciseMap, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exerciseMap, "Tư thế siêu nhân (Superman)"),
                        getEx(exerciseMap, "Burpees"),
                        getEx(exerciseMap, "Child's Pose")
                );
            } else {
                return listOfEx(
                        getEx(exerciseMap, "Arm Circles"),
                        getEx(exerciseMap, "Hip Circles"),
                        getEx(exerciseMap, "Cat-Cow"),
                        getEx(exerciseMap, "Squat gánh thân người (Bodyweight Squat)"),
                        getEx(exerciseMap, "Hít đất quỳ gối (Knee Push-up)"),
                        getEx(exerciseMap, "Chùng chân bước lùi (Reverse Lunge)"),
                        getEx(exerciseMap, "Cầu mông (Glute Bridge)"),
                        getEx(exerciseMap, "Plank khuỷu tay"),
                        getEx(exerciseMap, "Standing Quad Stretch"),
                        getEx(exerciseMap, "Child's Pose")
                );
            }
        }

        if (muscle.contains("nguc") || muscle.contains("tay") || title.contains("nguc") || title.contains("tay")) {
            return listOfEx(
                    getEx(exerciseMap, "Arm Circles"),
                    getEx(exerciseMap, "Hít đất tiêu chuẩn (Standard Push-up)"),
                    getEx(exerciseMap, "Hít đất kim cương (Diamond Push-up)"),
                    getEx(exerciseMap, "Hít đất tay rộng (Wide Push-up)"),
                    getEx(exerciseMap, "Nhún tay sau trên ghế (Chair Dips)"),
                    getEx(exerciseMap, "Pike Push-up"),
                    getEx(exerciseMap, "Chest Opener")
            );
        }

        if (muscle.contains("chan") || muscle.contains("mong") || title.contains("chan") || title.contains("mong")) {
            return listOfEx(
                    getEx(exerciseMap, "Hip Circles"),
                    getEx(exerciseMap, "Squat gánh thân người (Bodyweight Squat)"),
                    getEx(exerciseMap, "Bulgarian Split Squat"),
                    getEx(exerciseMap, "Chùng chân bước lùi (Reverse Lunge)"),
                    getEx(exerciseMap, "Sumo Squat"),
                    getEx(exerciseMap, "Nâng gót chân (Calf Raise)"),
                    getEx(exerciseMap, "Cầu mông (Glute Bridge)"),
                    getEx(exerciseMap, "Standing Quad Stretch")
            );
        }

        if (muscle.contains("bung") || goal.contains("abs") || title.contains("bung")) {
            if (level.contains("hard")) {
                return listOfEx(
                        getEx(exerciseMap, "Cat-Cow"),
                        getEx(exerciseMap, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exerciseMap, "V-up gập bụng"),
                        getEx(exerciseMap, "Hollow Hold"),
                        getEx(exerciseMap, "Plank nghiêng nâng hông"),
                        getEx(exerciseMap, "Plank cao giữ thẳng tay"),
                        getEx(exerciseMap, "Child's Pose")
                );
            } else if (level.contains("medium")) {
                return listOfEx(
                        getEx(exerciseMap, "Cat-Cow"),
                        getEx(exerciseMap, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exerciseMap, "Nâng chân nằm ngửa (Leg Raise)"),
                        getEx(exerciseMap, "Gập bụng ngược (Reverse Crunch)"),
                        getEx(exerciseMap, "Plank khuỷu tay"),
                        getEx(exerciseMap, "Plank nghiêng"),
                        getEx(exerciseMap, "Child's Pose")
                );
            } else {
                return listOfEx(
                        getEx(exerciseMap, "Cat-Cow"),
                        getEx(exerciseMap, "Gập bụng cơ bản (Crunches)"),
                        getEx(exerciseMap, "Chạm gót chân (Heel Taps)"),
                        getEx(exerciseMap, "Bọ chết (Dead Bug)"),
                        getEx(exerciseMap, "Plank quỳ gối"),
                        getEx(exerciseMap, "Child's Pose")
                );
            }
        }

        return listOfEx(
                getEx(exerciseMap, "Arm Circles"),
                getEx(exerciseMap, "Nhảy dang tay chân (Jumping Jacks)"),
                getEx(exerciseMap, "Squat gánh thân người (Bodyweight Squat)"),
                getEx(exerciseMap, "Hít đất tiêu chuẩn (Standard Push-up)"),
                getEx(exerciseMap, "Chùng chân bước lùi (Reverse Lunge)"),
                getEx(exerciseMap, "Plank khuỷu tay"),
                getEx(exerciseMap, "Tư thế siêu nhân (Superman)"),
                getEx(exerciseMap, "Child's Pose")
        );
    }

    private int extractDayIndex(String text) {
        if (text == null) return 1;
        var matcher = Pattern.compile("ngay\\s*(\\d+)").matcher(text);
        if (matcher.find()) {
            try { return Integer.parseInt(matcher.group(1)); } catch (Exception ignored) {}
        }
        return 1;
    }

    private String getDayTypeName(int dayIndex) {
        int posInWeek = (dayIndex - 1) % 7 + 1;
        switch ((posInWeek - 1) % 3) {
            case 0: return "Ngực & Tay";
            case 1: return "Lưng & Bụng";
            default: return "Chân";
        }
    }

    private List<Exercise> buildChallengeExercises(String typeName, boolean isCycle2, Map<String, Exercise> exercises) {
        List<Exercise> list = new ArrayList<>();
        addIfNotNull(list, getEx(exercises, "Arm Circles"));
        addIfNotNull(list, getEx(exercises, "Cat-Cow"));

        if ("Ngực & Tay".equals(typeName)) {
            if (!isCycle2) {
                addIfNotNull(list, getEx(exercises, "Hít đất vào tường"));
                addIfNotNull(list, getEx(exercises, "Hít đất quỳ gối (Knee Push-up)"));
                addIfNotNull(list, getEx(exercises, "Triceps Dips trên sàn"));
                addIfNotNull(list, getEx(exercises, "Hít đất kim cương (Diamond Push-up)"));
                addIfNotNull(list, getEx(exercises, "Plank chạm vai"));
            } else {
                addIfNotNull(list, getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"));
                addIfNotNull(list, getEx(exercises, "Hít đất lệch tay"));
                addIfNotNull(list, getEx(exercises, "Nhún tay sau trên ghế (Chair Dips)"));
                addIfNotNull(list, getEx(exercises, "Pike Push-up"));
                addIfNotNull(list, getEx(exercises, "Plank khuỷu tay"));
            }
        } else if ("Lưng & Bụng".equals(typeName)) {
            if (!isCycle2) {
                addIfNotNull(list, getEx(exercises, "Chó chim (Bird Dog)"));
                addIfNotNull(list, getEx(exercises, "Tư thế siêu nhân (Superman)"));
                addIfNotNull(list, getEx(exercises, "Bọ chết (Dead Bug)"));
                addIfNotNull(list, getEx(exercises, "Chạm gót chân (Heel Taps)"));
                addIfNotNull(list, getEx(exercises, "Gập bụng cơ bản (Crunches)"));
            } else {
                addIfNotNull(list, getEx(exercises, "Gập bụng đạp xe (Bicycle Crunches)"));
                addIfNotNull(list, getEx(exercises, "Leo núi tại chỗ (Mountain Climber)"));
                addIfNotNull(list, getEx(exercises, "Plank cao giữ thẳng tay"));
                addIfNotNull(list, getEx(exercises, "Nâng chân nằm ngửa (Leg Raise)"));
                addIfNotNull(list, getEx(exercises, "Plank nghiêng nâng hông"));
            }
        } else {
            if (!isCycle2) {
                addIfNotNull(list, getEx(exercises, "Squat gánh thân người (Bodyweight Squat)"));
                addIfNotNull(list, getEx(exercises, "Sumo Squat"));
                addIfNotNull(list, getEx(exercises, "Chùng chân bước lùi (Reverse Lunge)"));
                addIfNotNull(list, getEx(exercises, "Nâng gót chân (Calf Raise)"));
                addIfNotNull(list, getEx(exercises, "Cầu mông (Glute Bridge)"));
            } else {
                addIfNotNull(list, getEx(exercises, "Squat bật nhảy (Jump Squat)"));
                addIfNotNull(list, getEx(exercises, "Lateral Lunge"));
                addIfNotNull(list, getEx(exercises, "Wall Sit"));
                addIfNotNull(list, getEx(exercises, "Bulgarian Split Squat"));
                addIfNotNull(list, getEx(exercises, "Đá chân sau (Donkey Kick)"));
            }
        }

        addIfNotNull(list, getEx(exercises, "Chest Opener"));
        addIfNotNull(list, getEx(exercises, "Child's Pose"));
        return list;
    }

    private void addIfNotNull(List<Exercise> list, Exercise e) {
        if (e != null) list.add(e);
    }

    private List<Exercise> listOfEx(Exercise... array) {
        List<Exercise> list = new ArrayList<>();
        for (Exercise e : array) {
            if (e != null) list.add(e);
        }
        return list;
    }

    private Map<String, Exercise> loadExerciseMap() {
        Map<String, Exercise> map = new HashMap<>();
        for (Exercise e : exerciseRepository.findAll()) {
            map.put(e.getName(), e);
            map.put(stripAccents(e.getName()).toLowerCase(Locale.US), e);
        }
        return map;
    }

    private Exercise getEx(Map<String, Exercise> map, String name) {
        Exercise e = map.get(name);
        if (e == null) {
            e = map.get(stripAccents(name).toLowerCase(Locale.US));
        }
        if (e == null) {
            e = exerciseRepository.findFirstByName(name).orElse(null);
        }
        return e;
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}
