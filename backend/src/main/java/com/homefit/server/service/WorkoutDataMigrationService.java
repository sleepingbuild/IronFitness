package com.homefit.server.service;

import com.homefit.server.domain.ContentRelease;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.domain.WorkoutItem;
import com.homefit.server.repository.ContentReleaseRepository;
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
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class WorkoutDataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(WorkoutDataMigrationService.class);

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final ContentReleaseRepository contentReleaseRepository;
    private final DataSeeder dataSeeder;

    public WorkoutDataMigrationService(WorkoutRepository workoutRepository,
                                       ExerciseRepository exerciseRepository,
                                       ContentReleaseRepository contentReleaseRepository,
                                       DataSeeder dataSeeder) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.contentReleaseRepository = contentReleaseRepository;
        this.dataSeeder = dataSeeder;
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
        // Đảm bảo bảng exercises đã có đủ 95 bài tập mẫu trước
        try {
            dataSeeder.run(null);
        } catch (Exception e) {
            log.warn("Lỗi chạy dataSeeder: " + e.getMessage());
        }

        List<Exercise> allDbExercises = exerciseRepository.findAll();
        if (allDbExercises.isEmpty()) {
            log.warn("Bảng exercises rỗng. Không thể phân bổ bài tập con.");
            return 0;
        }

        // Phân loại danh sách bài tập theo nhóm cơ để làm fallback
        List<Exercise> absFallback = new ArrayList<>();
        List<Exercise> chestFallback = new ArrayList<>();
        List<Exercise> legFallback = new ArrayList<>();
        List<Exercise> backFallback = new ArrayList<>();
        List<Exercise> fullFallback = new ArrayList<>();

        for (Exercise e : allDbExercises) {
            String name = stripAccents(e.getName()).toLowerCase(Locale.US);
            if (name.contains("bung") || name.contains("crunch") || name.contains("plank") || name.contains("leg raise") || name.contains("v-up")) {
                absFallback.add(e);
            } else if (name.contains("nguc") || name.contains("push-up") || name.contains("hit dat") || name.contains("dips") || name.contains("tay")) {
                chestFallback.add(e);
            } else if (name.contains("chan") || name.contains("squat") || name.contains("lunge") || name.contains("mong") || name.contains("calf")) {
                legFallback.add(e);
            } else if (name.contains("lung") || name.contains("superman") || name.contains("bird dog") || name.contains("back")) {
                backFallback.add(e);
            } else {
                fullFallback.add(e);
            }
        }
        if (absFallback.isEmpty()) absFallback = allDbExercises;
        if (chestFallback.isEmpty()) chestFallback = allDbExercises;
        if (legFallback.isEmpty()) legFallback = allDbExercises;
        if (backFallback.isEmpty()) backFallback = allDbExercises;
        if (fullFallback.isEmpty()) fullFallback = allDbExercises;

        List<Workout> allWorkouts = workoutRepository.findAll();
        if (allWorkouts.isEmpty()) {
            return 0;
        }

        int updatedCount = 0;
        for (Workout w : allWorkouts) {
            if (w.getItems() != null && !w.getItems().isEmpty()) {
                continue; // Bỏ qua nếu buổi tập này đã có bài tập
            }

            List<Exercise> exList = generateExercisesForWorkout(w, allDbExercises, absFallback, chestFallback, legFallback, backFallback, fullFallback);
            if (exList != null && !exList.isEmpty()) {
                populateItemsForWorkout(w, exList);
                w.setVersion(w.getVersion() + 1);
                workoutRepository.save(w);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            try {
                int latestVer = contentReleaseRepository.findTopByOrderByVersionDesc()
                        .map(ContentRelease::getVersion)
                        .orElse(1);
                ContentRelease release = new ContentRelease();
                release.setVersion(latestVer + 1);
                release.setNotes("Tự động đồng bộ bài tập khởi tạo cho " + updatedCount + " buổi tập.");
                release.setCreatedBy("admin@homefit.com");
                contentReleaseRepository.save(release);
            } catch (Exception e) {
                log.warn("Không thể tạo bản ghi content_release: " + e.getMessage());
            }
        }

        log.info("Đã nạp bài tập khởi tạo thành công cho {} buổi tập trống.", updatedCount);
        return updatedCount;
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

    private List<Exercise> generateExercisesForWorkout(Workout w,
                                                      List<Exercise> allDbExercises,
                                                      List<Exercise> absFallback,
                                                      List<Exercise> chestFallback,
                                                      List<Exercise> legFallback,
                                                      List<Exercise> backFallback,
                                                      List<Exercise> fullFallback) {
        String title = stripAccents(w.getTitleVi()).toLowerCase(Locale.US);
        String goal = stripAccents(w.getGoal() != null ? w.getGoal() : "").toLowerCase(Locale.US);
        String muscle = stripAccents(w.getPrimaryMuscle() != null ? w.getPrimaryMuscle() : "").toLowerCase(Locale.US);
        String level = stripAccents(w.getLevel() != null ? w.getLevel() : "").toLowerCase(Locale.US);
        String type = stripAccents(w.getType() != null ? w.getType() : "").toLowerCase(Locale.US);

        List<String> targetNames;

        // 1. Thử thách hoặc Tên chứa Ngày
        if (type.contains("challenge") || goal.contains("challenge") || title.contains("thu thach") || title.contains("ngay ")) {
            int dayIndex = extractDayIndex(title);
            int posInWeek = (dayIndex - 1) % 7 + 1;
            boolean isCycle2 = posInWeek > 3;

            if ((posInWeek - 1) % 3 == 0) {
                // Ngực & Tay
                if (!isCycle2) {
                    targetNames = List.of("Arm Circles", "Cat-Cow", "Hít đất vào tường", "Hít đất quỳ gối (Knee Push-up)", "Triceps Dips trên sàn", "Hít đất kim cương (Diamond Push-up)", "Plank chạm vai", "Chest Opener", "Child's Pose");
                } else {
                    targetNames = List.of("Arm Circles", "Cat-Cow", "Hít đất tiêu chuẩn (Standard Push-up)", "Hít đất lệch tay", "Nhún tay sau trên ghế (Chair Dips)", "Pike Push-up", "Plank khuỷu tay", "Chest Opener", "Child's Pose");
                }
            } else if ((posInWeek - 1) % 3 == 1) {
                // Lưng & Bụng
                if (!isCycle2) {
                    targetNames = List.of("Arm Circles", "Cat-Cow", "Chó chim (Bird Dog)", "Tư thế siêu nhân (Superman)", "Bọ chết (Dead Bug)", "Chạm gót chân (Heel Taps)", "Gập bụng cơ bản (Crunches)", "Child's Pose");
                } else {
                    targetNames = List.of("Arm Circles", "Cat-Cow", "Gập bụng đạp xe (Bicycle Crunches)", "Leo núi tại chỗ (Mountain Climber)", "Plank cao giữ thẳng tay", "Nâng chân nằm ngửa (Leg Raise)", "Plank nghiêng nâng hông", "Child's Pose");
                }
            } else {
                // Chân
                if (!isCycle2) {
                    targetNames = List.of("Hip Circles", "Cat-Cow", "Squat gánh thân người (Bodyweight Squat)", "Sumo Squat", "Chùng chân bước lùi (Reverse Lunge)", "Nâng gót chân (Calf Raise)", "Cầu mông (Glute Bridge)", "Standing Quad Stretch", "Child's Pose");
                } else {
                    targetNames = List.of("Hip Circles", "Cat-Cow", "Squat bật nhảy (Jump Squat)", "Lateral Lunge", "Wall Sit", "Bulgarian Split Squat", "Đá chân sau (Donkey Kick)", "Standing Quad Stretch", "Child's Pose");
                }
            }
        }
        // 2. Bụng (Abs)
        else if (muscle.contains("bung") || goal.contains("abs") || title.contains("bung")) {
            if (level.contains("hard") || title.contains("kho")) {
                targetNames = List.of("Cat-Cow", "Gập bụng đạp xe (Bicycle Crunches)", "V-up gập bụng", "Hollow Hold", "Plank nghiêng nâng hông", "Plank cao giữ thẳng tay", "Bọ chết (Dead Bug)", "Child's Pose");
            } else if (level.contains("medium") || title.contains("trung binh")) {
                targetNames = List.of("Cat-Cow", "Gập bụng đạp xe (Bicycle Crunches)", "Nâng chân nằm ngửa (Leg Raise)", "Gập bụng ngược (Reverse Crunch)", "Plank khuỷu tay", "Plank nghiêng", "Child's Pose");
            } else {
                targetNames = List.of("Cat-Cow", "Gập bụng cơ bản (Crunches)", "Chạm gót chân (Heel Taps)", "Bọ chết (Dead Bug)", "Plank quỳ gối", "Arm Circles", "Child's Pose");
            }
        }
        // 3. Ngực & Tay
        else if (muscle.contains("nguc") || muscle.contains("tay") || title.contains("nguc") || title.contains("tay")) {
            targetNames = List.of("Arm Circles", "Cat-Cow", "Hít đất tiêu chuẩn (Standard Push-up)", "Hít đất kim cương (Diamond Push-up)", "Hít đất tay rộng (Wide Push-up)", "Nhún tay sau trên ghế (Chair Dips)", "Pike Push-up", "Plank chạm vai", "Chest Opener", "Child's Pose");
        }
        // 4. Chân & Mông
        else if (muscle.contains("chan") || muscle.contains("mong") || title.contains("chan") || title.contains("mong")) {
            targetNames = List.of("Hip Circles", "Cat-Cow", "Squat gánh thân người (Bodyweight Squat)", "Bulgarian Split Squat", "Chùng chân bước lùi (Reverse Lunge)", "Sumo Squat", "Nâng gót chân (Calf Raise)", "Cầu mông (Glute Bridge)", "Standing Quad Stretch", "Child's Pose");
        }
        // 5. Tabata
        else if (type.contains("tabata") || goal.contains("tabata") || title.contains("tabata")) {
            targetNames = List.of("Nhảy dang tay chân (Jumping Jacks)", "Squat bật nhảy (Jump Squat)", "Leo núi tại chỗ (Mountain Climber)", "Burpees", "Arm Circles", "Child's Pose");
        }
        // 6. Stretch
        else if (type.contains("stretch") || goal.contains("stretch") || title.contains("keo gian")) {
            targetNames = List.of("Cat-Cow", "Child's Pose", "Chest Opener", "Standing Hamstring Stretch", "Standing Quad Stretch", "Hip Circles");
        }
        // 7. Fat Loss / HIIT / Toàn thân / Calo
        else {
            if (level.contains("hard") || title.contains("cao")) {
                targetNames = List.of("Nhảy dang tay chân (Jumping Jacks)", "Squat bật nhảy (Jump Squat)", "Hít đất tiêu chuẩn (Standard Push-up)", "Leo núi tại chỗ (Mountain Climber)", "Burpees", "Gập bụng đạp xe (Bicycle Crunches)", "Plank khuỷu tay", "Standing Quad Stretch", "Child's Pose");
            } else if (level.contains("medium") || title.contains("vua")) {
                targetNames = List.of("Nhảy dang tay chân (Jumping Jacks)", "Arm Circles", "Squat bật nhảy (Jump Squat)", "Hít đất tiêu chuẩn (Standard Push-up)", "Chùng chân bước lùi (Reverse Lunge)", "Leo núi tại chỗ (Mountain Climber)", "Gập bụng đạp xe (Bicycle Crunches)", "Tư thế siêu nhân (Superman)", "Burpees", "Child's Pose");
            } else {
                targetNames = List.of("Arm Circles", "Hip Circles", "Cat-Cow", "Squat gánh thân người (Bodyweight Squat)", "Hít đất quỳ gối (Knee Push-up)", "Chùng chân bước lùi (Reverse Lunge)", "Cầu mông (Glute Bridge)", "Plank khuỷu tay", "Standing Quad Stretch", "Child's Pose");
            }
        }

        List<Exercise> result = new ArrayList<>();
        List<Exercise> fallbackCategory = fullFallback;
        if (muscle.contains("bung") || goal.contains("abs")) fallbackCategory = absFallback;
        else if (muscle.contains("nguc") || muscle.contains("tay")) fallbackCategory = chestFallback;
        else if (muscle.contains("chan") || muscle.contains("mong")) fallbackCategory = legFallback;
        else if (muscle.contains("lung")) fallbackCategory = backFallback;

        for (String name : targetNames) {
            Exercise ex = findExercise(name, allDbExercises, fallbackCategory);
            if (ex != null && !result.contains(ex)) {
                result.add(ex);
            }
        }

        // Đảm bảo mỗi buổi tập rỗng luôn được nạp đủ từ 7 đến 14 bài
        if (result.size() < 7) {
            for (Exercise ex : fallbackCategory) {
                if (!result.contains(ex)) {
                    result.add(ex);
                }
                if (result.size() >= 8) break;
            }
        }

        return result;
    }

    private Exercise findExercise(String name, List<Exercise> allDbExercises, List<Exercise> categoryFallback) {
        if (allDbExercises == null || allDbExercises.isEmpty()) return null;

        String targetNorm = stripAccents(name).toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");

        for (Exercise e : allDbExercises) {
            String dbNorm = stripAccents(e.getName()).toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
            if (dbNorm.equalsIgnoreCase(targetNorm) || dbNorm.contains(targetNorm) || targetNorm.contains(dbNorm)) {
                return e;
            }
        }

        for (Exercise e : allDbExercises) {
            String dbNorm = stripAccents(e.getName()).toLowerCase(Locale.US);
            if (targetNorm.contains("squat") && dbNorm.contains("squat")) return e;
            if (targetNorm.contains("hit dat") && (dbNorm.contains("hit dat") || dbNorm.contains("push-up") || dbNorm.contains("chong day"))) return e;
            if (targetNorm.contains("gap bung") && (dbNorm.contains("gap bung") || dbNorm.contains("crunch"))) return e;
            if (targetNorm.contains("plank") && dbNorm.contains("plank")) return e;
            if (targetNorm.contains("lunge") && dbNorm.contains("lunge")) return e;
            if (targetNorm.contains("burpee") && dbNorm.contains("burpee")) return e;
            if (targetNorm.contains("jump") && (dbNorm.contains("jump") || dbNorm.contains("nhay"))) return e;
            if (targetNorm.contains("stretch") && (dbNorm.contains("stretch") || dbNorm.contains("gian"))) return e;
        }

        if (categoryFallback != null && !categoryFallback.isEmpty()) {
            int hash = Math.abs(name.hashCode());
            return categoryFallback.get(hash % categoryFallback.size());
        }

        int hash = Math.abs(name.hashCode());
        return allDbExercises.get(hash % allDbExercises.size());
    }

    private int extractDayIndex(String text) {
        if (text == null) return 1;
        var matcher = Pattern.compile("ngay\\s*(\\d+)").matcher(text);
        if (matcher.find()) {
            try { return Integer.parseInt(matcher.group(1)); } catch (Exception ignored) {}
        }
        return 1;
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}
