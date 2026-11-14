package com.homefit.server.service;

import com.homefit.server.domain.Category;
import com.homefit.server.domain.ChallengeProgram;
import com.homefit.server.domain.ChallengeSession;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.MediaType;
import com.homefit.server.domain.Workout;
import com.homefit.server.domain.WorkoutItem;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ChallengeProgramRepository;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tự động đồng bộ và khởi tạo bộ dữ liệu chuẩn (Categories, Exercises, Workouts, WorkoutItems, ChallengePrograms, ChallengeSessions)
 * từ phía Android App sang Backend/TiDB Cloud. Đảm bảo idempotent (chạy lại không trùng lặp).
 */
@Component
@Order(10)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final ChallengeProgramRepository challengeProgramRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      ExerciseRepository exerciseRepository,
                      WorkoutRepository workoutRepository,
                      ChallengeProgramRepository challengeProgramRepository) {
        this.categoryRepository = categoryRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.challengeProgramRepository = challengeProgramRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu quy trình nạp đồng bộ dữ liệu chuẩn từ Android sang Backend...");

        Map<String, Category> categories = seedCategories();
        Map<String, Exercise> exerciseMap = seedExercises(categories);

        seedPopularWorkouts(exerciseMap);
        seedBodyPartWorkouts(exerciseMap);
        seedSpecialWorkouts(exerciseMap);

        seedChallengePrograms(exerciseMap);

        autoAssignRealFitnessImages();

        log.info("Đã đồng bộ hóa dữ liệu thành công! Tổng bài tập: {}, Tổng buổi tập: {}",
                exerciseRepository.count(), workoutRepository.count());
    }

    private void autoAssignRealFitnessImages() {
        List<Exercise> allExercises = exerciseRepository.findAll();
        int exUpdated = 0;
        for (Exercise e : allExercises) {
            String url = e.getMediaUrl();
            if (url == null || url.trim().isEmpty() || url.contains("placehold.co") || url.contains("placeholder") || url.contains("via.placeholder.com")) {
                e.setMediaUrl(getRealFitnessMediaUrlForExercise(e.getName(), e.getCategoryId()));
                exerciseRepository.save(e);
                exUpdated++;
            }
        }

        List<Workout> allWorkouts = workoutRepository.findAll();
        int woUpdated = 0;
        for (Workout w : allWorkouts) {
            String cover = w.getCoverImageUrl();
            if (cover == null || cover.trim().isEmpty() || cover.contains("placehold.co") || cover.contains("placeholder") || cover.contains("via.placeholder.com")) {
                w.setCoverImageUrl(getRealFitnessCoverUrlForWorkout(w.getType(), w.getGoal(), w.getPrimaryMuscle()));
                workoutRepository.save(w);
                woUpdated++;
            }
        }

        log.info("Tự động gán bộ ảnh thể hình thực tế thành công: {} Bài tập, {} Buổi tập", exUpdated, woUpdated);
    }

    private String getRealFitnessMediaUrlForExercise(String name, Long categoryId) {
        String n = name != null ? name.toLowerCase() : "";
        if (n.contains("plank") || n.contains("gập bụng") || n.contains("crunch") || n.contains("mountain climber") || n.contains("leg raise")) {
            return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600";
        }
        if (n.contains("hít đất") || n.contains("push-up") || n.contains("pushup") || n.contains("ngực")) {
            return "https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600";
        }
        if (n.contains("tay") || n.contains("dip") || n.contains("curl") || n.contains("tricep") || n.contains("bicep")) {
            return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=600";
        }
        if (n.contains("squat") || n.contains("lunge") || n.contains("chân") || n.contains("mông") || n.contains("calf")) {
            return "https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=600";
        }
        if (n.contains("lưng") || n.contains("superman") || n.contains("bird dog") || n.contains("row") || n.contains("cat-cow")) {
            return "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=600";
        }
        if (n.contains("stretch") || n.contains("giãn") || n.contains("kéo")) {
            return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600";
        }
        if (n.contains("jumping") || n.contains("burpee") || n.contains("tabata") || n.contains("hiit")) {
            return "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=600";
        }

        if (categoryId != null) {
            if (categoryId == 1L) return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600";
            if (categoryId == 2L) return "https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600";
            if (categoryId == 3L) return "https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=600";
            if (categoryId == 4L) return "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=600";
            if (categoryId == 5L) return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=600";
            if (categoryId == 6L) return "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=600";
        }
        return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600";
    }

    private String getRealFitnessCoverUrlForWorkout(String type, String goal, String primaryMuscle) {
        if ("tabata".equalsIgnoreCase(type) || "tabata".equalsIgnoreCase(goal)) {
            return "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=800";
        }
        if ("stretch".equalsIgnoreCase(type) || "stretch".equalsIgnoreCase(goal)) {
            return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800";
        }
        if ("fatloss".equalsIgnoreCase(goal)) {
            return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
        }
        if (primaryMuscle != null) {
            String m = primaryMuscle.toLowerCase();
            if (m.contains("bụng") || m.contains("abs")) {
                return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800";
            }
            if (m.contains("ngực") || m.contains("chest")) {
                return "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800";
            }
            if (m.contains("tay") || m.contains("arms")) {
                return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800";
            }
            if (m.contains("chân") || m.contains("legs")) {
                return "https://images.unsplash.com/photo-1434596922112-19c563067271?w=800";
            }
            if (m.contains("lưng") || m.contains("back")) {
                return "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=800";
            }
        }
        return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
    }

    private Map<String, Category> seedCategories() {
        Map<String, String[]> defs = Map.of(
                "abs", new String[]{"Cơ Bụng", "1"},
                "chest", new String[]{"Cơ Ngực", "2"},
                "legs", new String[]{"Cơ Chân", "3"},
                "full_body", new String[]{"Toàn Thân", "4"},
                "arms", new String[]{"Cơ Tay", "5"},
                "back", new String[]{"Cơ Lưng", "6"}
        );

        Map<String, Category> result = new HashMap<>();
        defs.forEach((slug, val) -> {
            Category c = categoryRepository.findBySlug(slug).orElseGet(() -> {
                Category cat = new Category();
                cat.setSlug(slug);
                cat.setName(val[0]);
                cat.setSortOrder(Integer.parseInt(val[1]));
                cat.setActive(true);
                return categoryRepository.save(cat);
            });
            c.setActive(true);
            categoryRepository.save(c);
            result.put(slug, c);
        });
        return result;
    }

    private Map<String, Exercise> seedExercises(Map<String, Category> categories) {
        List<Object[]> data = List.of(
                // Bụng (ABS)
                new Object[]{"Gập bụng cơ bản (Crunches)", "abs", "Nằm ngửa, co gối, siết cơ bụng nâng vai khỏi sàn.", null, 15, 15},
                new Object[]{"Plank khuỷu tay", "abs", "Giữ thân người thẳng tựa trên khuỷu tay và mũi chân.", 45, null, 15},
                new Object[]{"Plank cao giữ thẳng tay", "abs", "Ở tư thế chống đẩy, giữ thẳng tay và siết chặt cơ lõi.", 45, null, 15},
                new Object[]{"Plank quỳ gối", "abs", "Ở tư thế plank chống gối xuống sàn cho người mới.", 30, null, 15},
                new Object[]{"Gập bụng đạp xe (Bicycle Crunches)", "abs", "Xoay người đưa khuỷu tay chạm gối đối diện.", null, 20, 15},
                new Object[]{"Nâng chân nằm ngửa (Leg Raise)", "abs", "Nằm ngửa, giữ chân thẳng và nâng vuông góc với sàn.", null, 12, 20},
                new Object[]{"Bọ chết (Dead Bug)", "abs", "Nằm ngửa, hạ đồng thời tay trái và chân phải ra xa.", null, 12, 15},
                new Object[]{"Chạm gót chân (Heel Taps)", "abs", "Nhiêng người lần lượt chạm tay vào gót chân.", 30, null, 15},
                new Object[]{"Gập bụng ngược (Reverse Crunch)", "abs", "Nằm ngửa, cuộn gối kéo hông lên cao.", null, 12, 15},
                new Object[]{"Plank nghiêng", "abs", "Tựa người trên 1 cẳng tay, giữ hông cao.", 30, null, 15},
                new Object[]{"Plank nghiêng nâng hông", "abs", "Ở tư thế plank nghiêng, hạ và nâng hông nhịp nhàng.", 30, null, 15},
                new Object[]{"Hollow Hold", "abs", "Nằm ngửa nâng nhẹ tay chân khỏi sàn, siết bụng.", 30, null, 15},
                new Object[]{"V-up gập bụng", "abs", "Nâng đồng thời chân và thân trên tạo hình chữ V.", null, 10, 20},

                // Ngực (CHEST)
                new Object[]{"Hít đất tiêu chuẩn (Standard Push-up)", "chest", "Hai tay rộng bằng vai, hạ ngực gần sát sàn rồi đẩy lên.", null, 12, 20},
                new Object[]{"Hít đất quỳ gối (Knee Push-up)", "chest", "Chống hai gối xuống sàn, hạ đẩy ngực.", null, 12, 15},
                new Object[]{"Hít đất tay rộng (Wide Push-up)", "chest", "Đặt hai bàn tay rộng hơn vai.", null, 10, 20},
                new Object[]{"Hít đất vào tường", "chest", "Chống tay vào tường đẩy ngực nhẹ nhàng.", null, 15, 15},
                new Object[]{"Hít đất lệch tay", "chest", "Hít đất với một tay đặt phía trên tay kia.", null, 10, 20},
                new Object[]{"Plank chạm vai", "chest", "Ở tư thế chống đẩy, luân phiên đưa tay chạm vai đối diện.", 30, null, 15},

                // Tay & Lưng (ARMS & BACK)
                new Object[]{"Hít đất kim cương (Diamond Push-up)", "arms", "Chụm ngón trỏ và ngón cái tạo hình kim cương.", null, 10, 25},
                new Object[]{"Nhún tay sau trên ghế (Chair Dips)", "arms", "Đặt hai tay lên gờ ghế phía sau, hạ hông rồi đẩy lên.", null, 12, 20},
                new Object[]{"Triceps Dips trên sàn", "arms", "Chống tay sau trên sàn, nâng hạ hông tập tay sau.", null, 12, 15},
                new Object[]{"Pike Push-up", "arms", "Nâng hông cao tạo hình chữ V ngược, hạ đẩy vai.", null, 10, 20},
                new Object[]{"Tư thế siêu nhân (Superman)", "back", "Nằm sấp, nâng đồng thời tay và chân khỏi sàn.", null, 12, 20},
                new Object[]{"Chó chim (Bird Dog)", "back", "Quỳ 4 chi, duỗi thẳng tay trái và chân phải.", null, 12, 15},
                new Object[]{"Reverse Snow Angel", "back", "Nằm sấp xoay hai tay từ hông lên qua đầu.", 30, null, 15},

                // Chân (LEGS)
                new Object[]{"Squat gánh thân người (Bodyweight Squat)", "legs", "Đứng rộng bằng vai, hạ hông ra sau rồi đứng thẳng.", null, 15, 20},
                new Object[]{"Squat bật nhảy (Jump Squat)", "legs", "Squat sau đó bật nhảy lên cao hết mức.", null, 12, 30},
                new Object[]{"Chùng chân bước lùi (Reverse Lunge)", "legs", "Bước một chân ra sau, hạ gối vuông góc 90 độ.", null, 12, 20},
                new Object[]{"Sumo Squat", "legs", "Đứng chân rộng hơn vai, mũi chân hướng ra ngoài 45 độ.", null, 15, 20},
                new Object[]{"Nâng gót chân (Calf Raise)", "legs", "Nhón hai gót chân lên cao hết cỡ.", 30, null, 15},
                new Object[]{"Cầu mông (Glute Bridge)", "legs", "Nằm ngửa, nhấn gót đẩy hông siết mông.", null, 15, 20},
                new Object[]{"Wall Sit", "legs", "Tựa lưng vào tường ở tư thế ngồi vuông góc.", 30, null, 20},
                new Object[]{"Bulgarian Split Squat", "legs", "Đặt 1 chân ra sau lên ghế, hạ gối squat.", null, 10, 20},
                new Object[]{"Lateral Lunge", "legs", "Bước rộng sang bên, hạ hông dồn lực vào 1 chân.", null, 12, 20},
                new Object[]{"Đá chân sau (Donkey Kick)", "legs", "Quỳ 4 chi, đá 1 chân lên trần nhà.", null, 15, 15},

                // Toàn thân (FULL_BODY & WARMUP & COOL-DOWN)
                new Object[]{"Nhảy dang tay chân (Jumping Jacks)", "full_body", "Bật nhảy dạng rộng chân đồng thời đưa tay qua đầu.", 45, null, 15},
                new Object[]{"Leo núi tại chỗ (Mountain Climber)", "full_body", "Tư thế chống đẩy, luân phiên co gối về phía ngực.", 45, null, 20},
                new Object[]{"Burpees", "full_body", "Kết hợp ngồi xổm, chống đẩy và bật nhảy cao.", null, 10, 30},
                new Object[]{"Arm Circles", "full_body", "Xoay tròn hai cánh tay nhịp nhàng.", 30, null, 10},
                new Object[]{"Hip Circles", "full_body", "Xoay hông theo chiều kim đồng hồ.", 30, null, 10},
                new Object[]{"Cat-Cow", "full_body", "Quỳ 4 chi, võng và uốn cong lưng thả lỏng cột sống.", 30, null, 10},
                new Object[]{"Chest Opener", "full_body", "Đan tay sau lưng kéo căng lồng ngực.", 30, null, 10},
                new Object[]{"Child's Pose", "full_body", "Quỳ vươn dài hai tay về phía trước thả lỏng.", 30, null, 10},
                new Object[]{"Standing Quad Stretch", "full_body", "Đứng gập gối kéo gót chân về mông.", 30, null, 10},
                new Object[]{"Standing Hamstring Stretch", "full_body", "Gập người kéo căng cơ đùi sau.", 30, null, 10},
                new Object[]{"Figure-4 Stretch", "full_body", "Vắt chân hình chữ 4 kéo căng cơ mông.", 30, null, 10},
                new Object[]{"Kneeling Hip Flexor Stretch", "full_body", "Quỳ 1 chân vươn người kéo căng gập hông.", 30, null, 10},
                new Object[]{"Shoulder Cross-Body Stretch", "full_body", "Kéo ngang tay qua ngực thả lỏng vai.", 30, null, 10}
        );

        Map<String, Exercise> map = new HashMap<>();
        int sort = 1;
        for (Object[] row : data) {
            String name = (String) row[0];
            String catSlug = (String) row[1];
            String desc = (String) row[2];
            Integer dur = (Integer) row[3];
            Integer reps = (Integer) row[4];
            int rest = (int) row[5];

            Category cat = categories.getOrDefault(catSlug, categories.get("full_body"));

            Exercise e = exerciseRepository.findFirstByName(name).orElseGet(() -> {
                Exercise ex = new Exercise();
                ex.setName(name);
                ex.setCategoryId(cat.getId());
                return ex;
            });
            e.setCategoryId(cat.getId());
            e.setDescription(desc);
            e.setDurationSec(dur);
            e.setReps(reps);
            e.setRestSec(rest);
            e.setMediaType(MediaType.GIF);
            e.setMediaUrl("https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600");
            e.setSortOrder(sort++);
            e.setActive(true);

            exerciseRepository.save(e);
            map.put(name, e);
        }
        return map;
    }

    private void seedPopularWorkouts(Map<String, Exercise> exercises) {
        createOrUpdateWorkout("goal", "Đốt mỡ nhẹ nhàng", "Toàn thân", "easy", "fatloss", 30, 20, 20,
                listOfEx(
                        getEx(exercises, "Arm Circles"),
                        getEx(exercises, "Hip Circles"),
                        getEx(exercises, "Cat-Cow"),
                        getEx(exercises, "Squat gánh thân người (Bodyweight Squat)"),
                        getEx(exercises, "Hít đất quỳ gối (Knee Push-up)"),
                        getEx(exercises, "Chùng chân bước lùi (Reverse Lunge)"),
                        getEx(exercises, "Cầu mông (Glute Bridge)"),
                        getEx(exercises, "Plank khuỷu tay"),
                        getEx(exercises, "Standing Quad Stretch"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("goal", "HIIT toàn thân", "Toàn thân", "medium", "fatloss", 35, 20, 25,
                listOfEx(
                        getEx(exercises, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exercises, "Arm Circles"),
                        getEx(exercises, "Squat bật nhảy (Jump Squat)"),
                        getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exercises, "Chùng chân bước lùi (Reverse Lunge)"),
                        getEx(exercises, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exercises, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exercises, "Tư thế siêu nhân (Superman)"),
                        getEx(exercises, "Burpees"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("goal", "Đốt cháy calo toàn thân", "Toàn thân", "hard", "fatloss", 40, 15, 28,
                listOfEx(
                        getEx(exercises, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exercises, "Squat bật nhảy (Jump Squat)"),
                        getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exercises, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exercises, "Burpees"),
                        getEx(exercises, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exercises, "Standing Quad Stretch"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("goal", "Ngực & Tay", "Ngực", "medium", "muscle", 35, 15, 22,
                listOfEx(
                        getEx(exercises, "Arm Circles"),
                        getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exercises, "Hít đất kim cương (Diamond Push-up)"),
                        getEx(exercises, "Hít đất tay rộng (Wide Push-up)"),
                        getEx(exercises, "Nhún tay sau trên ghế (Chair Dips)"),
                        getEx(exercises, "Pike Push-up"),
                        getEx(exercises, "Chest Opener")
                ));

        createOrUpdateWorkout("goal", "Chân & Mông", "Chân", "medium", "muscle", 35, 15, 22,
                listOfEx(
                        getEx(exercises, "Hip Circles"),
                        getEx(exercises, "Squat gánh thân người (Bodyweight Squat)"),
                        getEx(exercises, "Bulgarian Split Squat"),
                        getEx(exercises, "Chùng chân bước lùi (Reverse Lunge)"),
                        getEx(exercises, "Sumo Squat"),
                        getEx(exercises, "Nâng gót chân (Calf Raise)"),
                        getEx(exercises, "Cầu mông (Glute Bridge)"),
                        getEx(exercises, "Standing Quad Stretch")
                ));

        createOrUpdateWorkout("goal", "Thân trên + Lõi", "Toàn thân", "medium", "muscle", 35, 15, 22,
                listOfEx(
                        getEx(exercises, "Arm Circles"),
                        getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exercises, "Tư thế siêu nhân (Superman)"),
                        getEx(exercises, "Pike Push-up"),
                        getEx(exercises, "Plank khuỷu tay"),
                        getEx(exercises, "Bọ chết (Dead Bug)"),
                        getEx(exercises, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("goal", "Toàn thân cân bằng", "Toàn thân", "easy", "keepfit", 30, 20, 15,
                listOfEx(
                        getEx(exercises, "Arm Circles"),
                        getEx(exercises, "Squat gánh thân người (Bodyweight Squat)"),
                        getEx(exercises, "Hít đất quỳ gối (Knee Push-up)"),
                        getEx(exercises, "Cầu mông (Glute Bridge)"),
                        getEx(exercises, "Chó chim (Bird Dog)"),
                        getEx(exercises, "Plank khuỷu tay"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("goal", "Toàn thân + Lõi", "Toàn thân", "medium", "keepfit", 35, 20, 20,
                listOfEx(
                        getEx(exercises, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exercises, "Squat gánh thân người (Bodyweight Squat)"),
                        getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exercises, "Chùng chân bước lùi (Reverse Lunge)"),
                        getEx(exercises, "Tư thế siêu nhân (Superman)"),
                        getEx(exercises, "Plank chạm vai"),
                        getEx(exercises, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("goal", "Sức bền toàn thân", "Toàn thân", "hard", "keepfit", 40, 15, 25,
                listOfEx(
                        getEx(exercises, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exercises, "Squat bật nhảy (Jump Squat)"),
                        getEx(exercises, "Hít đất tiêu chuẩn (Standard Push-up)"),
                        getEx(exercises, "Burpees"),
                        getEx(exercises, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exercises, "Child's Pose")
                ));
    }

    private void seedBodyPartWorkouts(Map<String, Exercise> exercises) {
        createOrUpdateWorkout("body_part", "Cơ Bụng Cấp Dễ", "Bụng", "easy", "abs", 30, 15, 15,
                listOfEx(
                        getEx(exercises, "Cat-Cow"),
                        getEx(exercises, "Gập bụng cơ bản (Crunches)"),
                        getEx(exercises, "Chạm gót chân (Heel Taps)"),
                        getEx(exercises, "Bọ chết (Dead Bug)"),
                        getEx(exercises, "Plank quỳ gối"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("body_part", "Cơ Bụng Cấp Trung Bình", "Bụng", "medium", "abs", 35, 15, 20,
                listOfEx(
                        getEx(exercises, "Cat-Cow"),
                        getEx(exercises, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exercises, "Nâng chân nằm ngửa (Leg Raise)"),
                        getEx(exercises, "Gập bụng ngược (Reverse Crunch)"),
                        getEx(exercises, "Plank khuỷu tay"),
                        getEx(exercises, "Plank nghiêng"),
                        getEx(exercises, "Child's Pose")
                ));

        createOrUpdateWorkout("body_part", "Cơ Bụng Cấp Khó", "Bụng", "hard", "abs", 40, 15, 25,
                listOfEx(
                        getEx(exercises, "Cat-Cow"),
                        getEx(exercises, "Gập bụng đạp xe (Bicycle Crunches)"),
                        getEx(exercises, "V-up gập bụng"),
                        getEx(exercises, "Hollow Hold"),
                        getEx(exercises, "Plank nghiêng nâng hông"),
                        getEx(exercises, "Plank cao giữ thẳng tay"),
                        getEx(exercises, "Child's Pose")
                ));
    }

    private void seedSpecialWorkouts(Map<String, Exercise> exercises) {
        createOrUpdateWorkout("tabata", "6 Phút Với Tabata Cường Độ Cao", "Toàn thân", "medium", "tabata", 20, 10, 15,
                listOfEx(
                        getEx(exercises, "Nhảy dang tay chân (Jumping Jacks)"),
                        getEx(exercises, "Squat bật nhảy (Jump Squat)"),
                        getEx(exercises, "Leo núi tại chỗ (Mountain Climber)"),
                        getEx(exercises, "Burpees")
                ));

        createOrUpdateWorkout("stretch", "Kéo Giãn Lưng & Giảm Mỏi Cột Sống", "Lưng", "easy", "stretch", 30, 10, 10,
                listOfEx(
                        getEx(exercises, "Cat-Cow"),
                        getEx(exercises, "Child's Pose"),
                        getEx(exercises, "Chest Opener"),
                        getEx(exercises, "Standing Hamstring Stretch")
                ));
    }

    private void seedChallengePrograms(Map<String, Exercise> exercises) {
        for (String level : List.of("easy", "medium", "hard")) {
            ChallengeProgram program = challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(level, "PUBLISHED")
                    .orElseGet(() -> {
                        ChallengeProgram p = new ChallengeProgram();
                        p.setLevel(level);
                        p.setStatus("PUBLISHED");
                        p.setVersion(1);
                        return challengeProgramRepository.save(p);
                    });

            List<ChallengeSession> sessionList = program.getSessions();
            if (sessionList == null) {
                sessionList = new ArrayList<>();
                program.setSessions(sessionList);
            }

            Map<Integer, ChallengeSession> existingDays = new HashMap<>();
            for (ChallengeSession s : sessionList) {
                existingDays.put(s.getDayIndex(), s);
            }

            int workSec = "hard".equalsIgnoreCase(level) ? 40 : ("medium".equalsIgnoreCase(level) ? 35 : 30);
            int restSec = "hard".equalsIgnoreCase(level) ? 15 : 20;

            for (int day = 1; day <= 28; day++) {
                boolean rest = (day % 7 == 0);
                int week = (day - 1) / 7 + 1;
                int posInWeek = (day - 1) % 7 + 1;

                ChallengeSession cs = existingDays.get(day);
                if (cs == null) {
                    cs = new ChallengeSession();
                    cs.setProgram(program);
                    cs.setDayIndex(day);
                    sessionList.add(cs);
                }
                cs.setWeekNum(week);
                cs.setRestDay(rest);

                if (rest) {
                    cs.setWorkoutId(null);
                } else {
                    String typeName = getDayTypeName(day);
                    String titleVi = "Thử thách " + getLevelVi(level) + " - Ngày " + day + " (" + typeName + ")";
                    List<Exercise> exList = buildChallengeExercises(typeName, posInWeek > 3, exercises);

                    Workout w = createOrUpdateWorkout("challenge", titleVi, typeName, level, "challenge", workSec, restSec, 20, exList);
                    cs.setWorkoutId(w.getId());
                }
            }
            challengeProgramRepository.save(program);
        }
    }

    private String getLevelVi(String level) {
        if ("hard".equalsIgnoreCase(level)) return "Khó";
        if ("medium".equalsIgnoreCase(level)) return "Trung bình";
        return "Dễ";
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

    private Workout createOrUpdateWorkout(String type, String titleVi, String primaryMuscle, String level, String goal,
                                           int workSec, int restSec, int durationMin, List<Exercise> exList) {
        Workout w = workoutRepository.findFirstByTitleVi(titleVi).orElseGet(() -> {
            Workout newW = new Workout();
            newW.setTitleVi(titleVi);
            return newW;
        });

        w.setType(type);
        w.setPrimaryMuscle(primaryMuscle);
        w.setLevel(level);
        w.setGoal(goal);
        w.setWorkSec(workSec);
        w.setRestSec(restSec);
        w.setStatus("PUBLISHED");
        w.setRounds(1);
        w.setRoundRestSec(20);

        if (w.getCoverImageUrl() == null || w.getCoverImageUrl().trim().isEmpty()) {
            w.setCoverImageUrl(getDefaultCoverImage(type, goal, primaryMuscle));
        }

        List<WorkoutItem> items = w.getItems();
        if (items == null) {
            items = new ArrayList<>();
            w.setItems(items);
        } else {
            items.clear();
        }

        int pos = 1;
        for (Exercise e : exList) {
            if (e == null) continue;
            WorkoutItem item = new WorkoutItem();
            item.setWorkout(w);
            item.setPosition(pos++);
            item.setExerciseId(e.getId());
            item.setBlock(pos <= 3 ? "A" : (pos > exList.size() - 2 ? "C" : "B"));
            item.setRoundNum(1);
            if (e.getDurationSec() != null) {
                item.setMeasure("time");
                item.setValue(e.getDurationSec());
            } else {
                item.setMeasure("reps");
                item.setValue(e.getReps() != null ? e.getReps() : 12);
            }
            item.setRestAfterSec(restSec);
            items.add(item);
        }

        return workoutRepository.save(w);
    }

    private String getDefaultCoverImage(String type, String goal, String primaryMuscle) {
        if ("tabata".equalsIgnoreCase(type) || "tabata".equalsIgnoreCase(goal)) {
            return "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=800";
        }
        if ("stretch".equalsIgnoreCase(type) || "stretch".equalsIgnoreCase(goal)) {
            return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800";
        }
        if ("fatloss".equalsIgnoreCase(goal)) {
            return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
        }
        if (primaryMuscle != null) {
            String m = primaryMuscle.toLowerCase();
            if (m.contains("bụng") || m.contains("abs")) {
                return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800";
            }
            if (m.contains("ngực") || m.contains("chest")) {
                return "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800";
            }
            if (m.contains("tay") || m.contains("arms")) {
                return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800";
            }
            if (m.contains("chân") || m.contains("legs")) {
                return "https://images.unsplash.com/photo-1434596922112-19c563067271?w=800";
            }
            if (m.contains("lưng") || m.contains("back")) {
                return "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=800";
            }
        }
        return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
    }

    private Exercise getEx(Map<String, Exercise> map, String name) {
        Exercise e = map.get(name);
        if (e == null) {
            e = exerciseRepository.findFirstByName(name).orElse(null);
        }
        return e;
    }
}
