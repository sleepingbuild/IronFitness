package com.homefit.server.service;

import com.homefit.server.domain.ContentRelease;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.Workout;
import com.homefit.server.domain.WorkoutItem;
import com.homefit.server.repository.ContentReleaseRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutItemRepository;
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
    private final WorkoutItemRepository workoutItemRepository;
    private final ExerciseRepository exerciseRepository;
    private final ContentReleaseRepository contentReleaseRepository;
    private final DataSeeder dataSeeder;

    public WorkoutDataMigrationService(WorkoutRepository workoutRepository,
                                       WorkoutItemRepository workoutItemRepository,
                                       ExerciseRepository exerciseRepository,
                                       ContentReleaseRepository contentReleaseRepository,
                                       DataSeeder dataSeeder) {
        this.workoutRepository = workoutRepository;
        this.workoutItemRepository = workoutItemRepository;
        this.exerciseRepository = exerciseRepository;
        this.contentReleaseRepository = contentReleaseRepository;
        this.dataSeeder = dataSeeder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        log.info("Bắt đầu kiểm tra và bổ sung tự động bài tập con cho toàn bộ các buổi tập trống trên TiDB Cloud...");
        try {
            populateAllEmptyWorkouts();
        } catch (Exception e) {
            log.error("Lỗi khi tự động bổ sung bài tập con: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String populateAllEmptyWorkouts() {
        // 1. Đảm bảo bảng exercises đã được nạp dữ liệu mẫu
        try {
            dataSeeder.run(null);
        } catch (Exception e) {
            log.warn("Cảnh báo DataSeeder: " + e.getMessage());
        }

        List<Exercise> allExercises = exerciseRepository.findAll();
        if (allExercises.isEmpty()) {
            throw new IllegalStateException("Thư viện bài tập (exercises) đang rỗng! Vui lòng nạp dữ liệu bài tập mẫu trước.");
        }

        List<Workout> workouts = workoutRepository.findAll();
        if (workouts.isEmpty()) {
            return "Không tìm thấy buổi tập nào trong cơ sở dữ liệu.";
        }

        // Phân loại danh sách bài tập theo nhóm cơ
        List<Exercise> absExercises = new ArrayList<>();
        List<Exercise> chestArmExercises = new ArrayList<>();
        List<Exercise> legExercises = new ArrayList<>();
        List<Exercise> backExercises = new ArrayList<>();
        List<Exercise> warmUpCoolDown = new ArrayList<>();

        for (Exercise e : allExercises) {
            String nameNorm = stripAccents(e.getName()).toLowerCase(Locale.US);
            if (nameNorm.contains("warm") || nameNorm.contains("circle") || nameNorm.contains("cat-cow") || nameNorm.contains("stretch") || nameNorm.contains("child") || nameNorm.contains("opener")) {
                warmUpCoolDown.add(e);
            }
            if (nameNorm.contains("bung") || nameNorm.contains("crunch") || nameNorm.contains("plank") || nameNorm.contains("leg raise") || nameNorm.contains("v-up") || nameNorm.contains("heel tap")) {
                absExercises.add(e);
            } else if (nameNorm.contains("nguc") || nameNorm.contains("tay") || nameNorm.contains("hit dat") || nameNorm.contains("push-up") || nameNorm.contains("dips") || nameNorm.contains("pike")) {
                chestArmExercises.add(e);
            } else if (nameNorm.contains("chan") || nameNorm.contains("mong") || nameNorm.contains("squat") || nameNorm.contains("lunge") || nameNorm.contains("calf") || nameNorm.contains("bridge")) {
                legExercises.add(e);
            } else if (nameNorm.contains("lung") || nameNorm.contains("superman") || nameNorm.contains("bird dog") || nameNorm.contains("back")) {
                backExercises.add(e);
            }
        }

        if (absExercises.isEmpty()) absExercises = allExercises;
        if (chestArmExercises.isEmpty()) chestArmExercises = allExercises;
        if (legExercises.isEmpty()) legExercises = allExercises;
        if (backExercises.isEmpty()) backExercises = allExercises;

        int updatedWorkouts = 0;
        int totalItemsCreated = 0;

        for (Workout w : workouts) {
            long count = workoutItemRepository.countByWorkoutId(w.getId());
            if (count >= 1) {
                continue; // Đã có bài tập con -> Bỏ qua
            }

            // Lọc danh sách bài tập phù hợp cho workout w
            List<Exercise> exList = selectExercisesForWorkout(w, allExercises, absExercises, chestArmExercises, legExercises, backExercises, warmUpCoolDown);
            if (exList.isEmpty()) {
                exList = new ArrayList<>(allExercises.subList(0, Math.min(10, allExercises.size())));
            }

            // Xóa sạch items cũ (nếu có) và tạo mới
            workoutItemRepository.deleteByWorkoutId(w.getId());
            workoutItemRepository.flush();

            List<WorkoutItem> newItems = new ArrayList<>();
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
                newItems.add(item);
            }

            if (!newItems.isEmpty()) {
                workoutItemRepository.saveAllAndFlush(newItems);
                w.setVersion(w.getVersion() + 1);
                workoutRepository.saveAndFlush(w);
                updatedWorkouts++;
                totalItemsCreated += newItems.size();
            }
        }

        if (updatedWorkouts > 0) {
            try {
                int latestVer = contentReleaseRepository.findTopByOrderByVersionDesc()
                        .map(ContentRelease::getVersion)
                        .orElse(1);
                ContentRelease release = new ContentRelease();
                release.setVersion(latestVer + 1);
                release.setNotes("Đồng bộ bài tập khởi tạo cho " + updatedWorkouts + " buổi tập.");
                release.setCreatedBy("admin@homefit.com");
                contentReleaseRepository.saveAndFlush(release);
            } catch (Exception e) {
                log.warn("Cảnh báo cập nhật contentRelease: " + e.getMessage());
            }
        }

        String msg = String.format(Locale.US, "Đã nạp bài tập cho %d / %d buổi tập (Tổng %d bài tập con được tạo)!",
                updatedWorkouts, workouts.size(), totalItemsCreated);
        log.info(msg);
        return msg;
    }

    private List<Exercise> selectExercisesForWorkout(Workout w,
                                                      List<Exercise> allExercises,
                                                      List<Exercise> absExercises,
                                                      List<Exercise> chestArmExercises,
                                                      List<Exercise> legExercises,
                                                      List<Exercise> backExercises,
                                                      List<Exercise> warmUpCoolDown) {
        String title = stripAccents(w.getTitleVi() != null ? w.getTitleVi() : "").toLowerCase(Locale.US);
        String muscle = stripAccents(w.getPrimaryMuscle() != null ? w.getPrimaryMuscle() : "").toLowerCase(Locale.US);

        List<Exercise> result = new ArrayList<>();

        // Lấy 2 bài khởi động
        for (Exercise e : warmUpCoolDown) {
            if (!result.contains(e)) result.add(e);
            if (result.size() >= 2) break;
        }

        // Chọn danh sách bài tập chính theo từ khóa
        List<Exercise> targetPool;
        if (title.contains("bung") || muscle.contains("bung") || title.contains("abs")) {
            targetPool = absExercises;
        } else if (title.contains("nguc") || title.contains("tay") || muscle.contains("nguc") || muscle.contains("tay") || muscle.contains("chest") || muscle.contains("arm")) {
            targetPool = chestArmExercises;
        } else if (title.contains("chan") || title.contains("mong") || muscle.contains("chan") || muscle.contains("mong") || muscle.contains("leg")) {
            targetPool = legExercises;
        } else if (title.contains("lung") || muscle.contains("lung") || muscle.contains("back")) {
            targetPool = backExercises;
        } else {
            targetPool = allExercises;
        }

        for (Exercise e : targetPool) {
            if (!result.contains(e)) {
                result.add(e);
            }
            if (result.size() >= 10) break;
        }

        // Nếu chưa đủ 8 bài, lấy thêm từ allExercises
        if (result.size() < 8) {
            for (Exercise e : allExercises) {
                if (!result.contains(e)) {
                    result.add(e);
                }
                if (result.size() >= 10) break;
            }
        }

        return result;
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}
