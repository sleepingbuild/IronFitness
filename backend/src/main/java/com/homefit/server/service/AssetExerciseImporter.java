package com.homefit.server.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.homefit.server.domain.Category;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.MediaType;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ExerciseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tự động đọc toàn bộ bài tập từ các tệp JSON/MD trong assets của Android App
 * và nạp đồng bộ vào CSDL MySQL (bảng exercises) để Admin Web có đủ 116+ bài tập.
 */
@Component
public class AssetExerciseImporter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetExerciseImporter.class);

    private final ExerciseRepository exerciseRepository;
    private final CategoryRepository categoryRepository;
    private final Gson gson = new Gson();

    public AssetExerciseImporter(ExerciseRepository exerciseRepository, CategoryRepository categoryRepository) {
        this.exerciseRepository = exerciseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        File assetsDir = new File("D:/IronFitness/android/app/src/main/assets/exercises");
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            assetsDir = new File("../android/app/src/main/assets/exercises");
        }
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            log.warn("Không tìm thấy thư mục assets/exercises để nạp bài tập.");
            return;
        }

        Map<String, Category> categoryMap = loadCategoryMap();
        Category defaultCategory = categoryMap.getOrDefault("full_body", categoryMap.values().stream().findFirst().orElse(null));

        File[] files = assetsDir.listFiles((dir, name) -> name.endsWith(".md") || name.endsWith(".json"));
        if (files == null) return;

        Set<String> existingNames = new HashSet<>();
        exerciseRepository.findAll().forEach(e -> existingNames.add(e.getName().trim().toLowerCase()));

        int importedCount = 0;
        int sortOrderCounter = (int) exerciseRepository.count() + 1;

        for (File file : files) {
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                JsonObject root = gson.fromJson(reader, JsonObject.class);
                if (root == null) continue;

                String groupKey = root.has("group") && !root.get("group").isJsonNull() ? root.get("group").getAsString().toLowerCase() : "";
                String groupName = root.has("group_name") && !root.get("group_name").isJsonNull() ? root.get("group_name").getAsString().toLowerCase() : "";

                Category category = resolveCategory(groupKey, groupName, categoryMap, defaultCategory);

                if (root.has("workouts") && root.get("workouts").isJsonArray()) {
                    JsonArray workouts = root.getAsJsonArray("workouts");
                    for (JsonElement elem : workouts) {
                        if (!elem.isJsonObject()) continue;
                        JsonObject w = elem.getAsJsonObject();

                        String name = w.has("name") && !w.get("name").isJsonNull() ? w.get("name").getAsString().trim() : "";
                        if (name.isEmpty() || existingNames.contains(name.toLowerCase())) {
                            continue;
                        }

                        Exercise exercise = new Exercise();
                        exercise.setName(name);
                        exercise.setCategoryId(category.getId());
                        
                        StringBuilder desc = new StringBuilder();
                        if (w.has("target") && !w.get("target").isJsonNull()) {
                            desc.append("Mục tiêu: ").append(w.get("target").getAsString()).append(". ");
                        }
                        if (w.has("instructions") && w.get("instructions").isJsonArray()) {
                            desc.append("Các bước: ");
                            for (JsonElement step : w.getAsJsonArray("instructions")) {
                                desc.append(step.getAsString()).append(" ");
                            }
                        }
                        exercise.setDescription(desc.toString().trim());

                        String repsSets = w.has("reps_sets") && !w.get("reps_sets").isJsonNull() ? w.get("reps_sets").getAsString() : "";
                        if (repsSets.toLowerCase().contains("giây") || repsSets.toLowerCase().contains("s") || repsSets.toLowerCase().contains("sec")) {
                            exercise.setDurationSec(extractNumber(repsSets, 30));
                            exercise.setReps(null);
                        } else {
                            exercise.setReps(extractNumber(repsSets, 12));
                            exercise.setDurationSec(null);
                        }

                        exercise.setRestSec(15);
                        exercise.setMediaType(MediaType.GIF);
                        exercise.setMediaUrl("https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600");
                        exercise.setSortOrder(sortOrderCounter++);
                        exercise.setActive(true);

                        exerciseRepository.save(exercise);
                        existingNames.add(name.toLowerCase());
                        importedCount++;
                    }
                }
            } catch (Exception e) {
                log.warn("Lỗi đọc file asset {}: {}", file.getName(), e.getMessage());
            }
        }

        if (importedCount > 0) {
            log.info("Đã tự động nạp thành công {} bài tập mới từ Android App assets. Tổng bài tập hiện có: {}", 
                    importedCount, exerciseRepository.count());
        }
    }

    private Map<String, Category> loadCategoryMap() {
        Map<String, Category> map = new HashMap<>();
        for (Category c : categoryRepository.findAll()) {
            map.put(c.getSlug().toLowerCase(), c);
        }
        return map;
    }

    private Category resolveCategory(String groupKey, String groupName, Map<String, Category> categoryMap, Category defaultCategory) {
        if (groupKey.contains("bung") || groupName.contains("bụng")) return categoryMap.getOrDefault("abs", defaultCategory);
        if (groupKey.contains("nguc") || groupName.contains("ngực")) return categoryMap.getOrDefault("chest", defaultCategory);
        if (groupKey.contains("chan") || groupName.contains("chân")) return categoryMap.getOrDefault("legs", defaultCategory);
        if (groupKey.contains("tay") || groupName.contains("tay")) return categoryMap.getOrDefault("arms", defaultCategory);
        if (groupKey.contains("lung") || groupName.contains("lưng")) return categoryMap.getOrDefault("back", defaultCategory);
        return categoryMap.getOrDefault("full_body", defaultCategory);
    }

    private int extractNumber(String text, int defaultValue) {
        try {
            String digits = text.replaceAll("[^0-9]", " ").trim();
            if (!digits.isEmpty()) {
                String[] parts = digits.split("\\s+");
                return Integer.parseInt(parts[0]);
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }
}
