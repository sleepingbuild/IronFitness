package com.homefit.server.service;

import com.homefit.server.admin.ExerciseForm;
import com.homefit.server.domain.Category;
import com.homefit.server.domain.Exercise;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ExerciseRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Quản lý bài tập trên trang quản trị. */
@Service
public class AdminExerciseService {

    public record Row(Long id, String categoryName, String name, String modeText, int restSec,
                      String mediaType, boolean active, int sortOrder, String updatedAtText) {
    }

    public record Option(Long id, String name) {
    }

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final ExerciseRepository exercises;
    private final CategoryRepository categories;

    public AdminExerciseService(ExerciseRepository exercises, CategoryRepository categories) {
        this.exercises = exercises;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<Row> listRows(Long categoryId, String sortBy, String sortDir) {
        Map<Long, String> names = categories.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        List<Exercise> list = categoryId == null
                ? exercises.findAllByOrderByCategoryIdAscSortOrderAscIdAsc()
                : exercises.findByCategoryIdOrderBySortOrderAscIdAsc(categoryId);

        List<Row> rows = list.stream().map(e -> new Row(e.getId(),
                names.getOrDefault(e.getCategoryId(), "?"), e.getName(), modeText(e), e.getRestSec(),
                e.getMediaType().name(), e.isActive(), e.getSortOrder(),
                e.getUpdatedAt() == null ? "" : DISPLAY.format(e.getUpdatedAt()))).collect(Collectors.toList());

        boolean isDesc = "desc".equalsIgnoreCase(sortDir);

        if ("name".equalsIgnoreCase(sortBy)) {
            rows.sort((r1, r2) -> isDesc ? r2.name().compareToIgnoreCase(r1.name()) : r1.name().compareToIgnoreCase(r2.name()));
        } else if ("id".equalsIgnoreCase(sortBy)) {
            rows.sort((r1, r2) -> isDesc ? Long.compare(r2.id(), r1.id()) : Long.compare(r1.id(), r2.id()));
        } else if ("category".equalsIgnoreCase(sortBy)) {
            rows.sort((r1, r2) -> isDesc ? r2.categoryName().compareToIgnoreCase(r1.categoryName()) : r1.categoryName().compareToIgnoreCase(r2.categoryName()));
        } else if ("status".equalsIgnoreCase(sortBy)) {
            rows.sort((r1, r2) -> isDesc ? Boolean.compare(r2.active(), r1.active()) : Boolean.compare(r1.active(), r2.active()));
        } else { // sortOrder default
            rows.sort((r1, r2) -> isDesc ? Integer.compare(r2.sortOrder(), r1.sortOrder()) : Integer.compare(r1.sortOrder(), r2.sortOrder()));
        }

        return rows;
    }

    @Transactional(readOnly = true)
    public List<Option> categoryOptions() {
        return categories.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(c -> new Option(c.getId(), c.isActive() ? c.getName() : c.getName() + " (đã ẩn)"))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean categoryExists(Long categoryId) {
        return categoryId != null && categories.existsById(categoryId);
    }

    @Transactional(readOnly = true)
    public ExerciseForm formFor(Long id) {
        return ExerciseForm.from(find(id));
    }

    @Transactional
    public void create(ExerciseForm form) {
        Exercise exercise = new Exercise();
        apply(exercise, form);
        exercises.save(exercise);
    }

    @Transactional
    public void update(Long id, ExerciseForm form) {
        Exercise exercise = find(id);
        apply(exercise, form);
        exercises.save(exercise);
    }

    @Transactional
    public void toggle(Long id) {
        Exercise exercise = find(id);
        exercise.setActive(!exercise.isActive());
        exercises.save(exercise);
    }
    
    @Transactional
    public int importCsv(MultipartFile file) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 9) continue;
                
                Exercise ex = new Exercise();
                ex.setCategoryId(Long.parseLong(parts[0].trim()));
                ex.setName(parts[1].trim());
                
                StringBuilder desc = new StringBuilder(parts[2]);
                for (int j = 3; j < parts.length - 6; j++) {
                    desc.append(",").append(parts[j]);
                }
                ex.setDescription(desc.toString().trim());
                
                String durStr = parts[parts.length - 6].trim();
                ex.setDurationSec(durStr.isEmpty() ? null : Integer.parseInt(durStr));
                
                String repsStr = parts[parts.length - 5].trim();
                ex.setReps(repsStr.isEmpty() ? null : Integer.parseInt(repsStr));
                
                String restStr = parts[parts.length - 4].trim();
                ex.setRestSec(restStr.isEmpty() ? 15 : Integer.parseInt(restStr));
                
                String typeStr = parts[parts.length - 3].trim();
                ex.setMediaType(com.homefit.server.domain.MediaType.valueOf(typeStr.toUpperCase()));
                
                ex.setMediaUrl(parts[parts.length - 2].trim());
                ex.setSortOrder(Integer.parseInt(parts[parts.length - 1].trim()));
                ex.setActive(true);
                
                exercises.save(ex);
                count++;
            }
        }
        return count;
    }
    
    public Map<String, String> checkMediaLink(String urlString) {
        Map<String, String> result = new HashMap<>();
        try {
            URL url = new URL(urlString);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            huc.setConnectTimeout(5000);
            huc.setReadTimeout(5000);
            int responseCode = huc.getResponseCode();
            if (responseCode == 200) {
                result.put("status", "OK");
                result.put("message", "200 OK");
            } else {
                result.put("status", "ERROR");
                result.put("message", "HTTP " + responseCode);
            }
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }

    private Exercise find(Long id) {
        return exercises.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));
    }

    private static String modeText(Exercise e) {
        return e.getDurationSec() != null ? e.getDurationSec() + " giây" : e.getReps() + " lần";
    }

    private static void apply(Exercise exercise, ExerciseForm form) {
        exercise.setCategoryId(form.getCategoryId());
        exercise.setName(form.getName().trim());
        String description = form.getDescription();
        exercise.setDescription(description == null || description.isBlank() ? null : description.trim());
        exercise.setMediaUrl(form.getMediaUrl().trim());
        exercise.setMediaType(form.getMediaType());
        if (form.getMode() == ExerciseForm.Mode.DURATION) {
            exercise.setDurationSec(form.getAmount());
            exercise.setReps(null);
        } else {
            exercise.setReps(form.getAmount());
            exercise.setDurationSec(null);
        }
        exercise.setRestSec(form.getRestSec());
        exercise.setSortOrder(form.getSortOrder());
        exercise.setActive(form.isActive());
    }
}
