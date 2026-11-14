package com.homefit.server.service;

import com.homefit.server.admin.ExerciseForm;
import com.homefit.server.domain.Category;
import com.homefit.server.domain.Exercise;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ExerciseRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    
    @Transactional(readOnly = true)
    public String exportCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,categoryId,description,durationSec,reps,restSec,mediaType,mediaUrl,sortOrder,active\n");
        List<Exercise> list = exercises.findAllByOrderByCategoryIdAscSortOrderAscIdAsc();
        for (Exercise e : list) {
            sb.append(e.getId()).append(",");
            sb.append(escapeCsv(e.getName())).append(",");
            sb.append(e.getCategoryId() != null ? e.getCategoryId() : "").append(",");
            sb.append(escapeCsv(e.getDescription() != null ? e.getDescription() : "")).append(",");
            sb.append(e.getDurationSec() != null ? e.getDurationSec() : "").append(",");
            sb.append(e.getReps() != null ? e.getReps() : "").append(",");
            sb.append(e.getRestSec()).append(",");
            sb.append(e.getMediaType() != null ? e.getMediaType().name() : "GIF").append(",");
            sb.append(escapeCsv(e.getMediaUrl() != null ? e.getMediaUrl() : "")).append(",");
            sb.append(e.getSortOrder()).append(",");
            sb.append(e.isActive()).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 2) continue;

                Long id = parseLong(tokens.get(0));
                String name = tokens.get(1).trim();
                Long categoryId = tokens.size() > 2 ? parseLong(tokens.get(2)) : 1L;
                String desc = tokens.size() > 3 ? tokens.get(3).trim() : "";
                Integer dur = tokens.size() > 4 ? parseInt(tokens.get(4)) : null;
                Integer reps = tokens.size() > 5 ? parseInt(tokens.get(5)) : null;
                Integer restSec = tokens.size() > 6 ? parseInt(tokens.get(6)) : 15;
                String mediaTypeStr = tokens.size() > 7 ? tokens.get(7).trim() : "GIF";
                String mediaUrl = tokens.size() > 8 ? tokens.get(8).trim() : "";
                Integer sortOrder = tokens.size() > 9 ? parseInt(tokens.get(9)) : 0;
                Boolean active = tokens.size() > 10 ? Boolean.parseBoolean(tokens.get(10).trim()) : true;

                Exercise ex = null;
                if (id != null) {
                    ex = exercises.findById(id).orElse(null);
                }
                if (ex == null && !name.isEmpty()) {
                    ex = exercises.findFirstByName(name).orElseGet(Exercise::new);
                }
                if (ex == null) {
                    ex = new Exercise();
                }

                if (categoryId != null) ex.setCategoryId(categoryId);
                if (!name.isEmpty()) ex.setName(name);
                ex.setDescription(desc.isEmpty() ? null : desc);
                ex.setDurationSec(dur);
                ex.setReps(reps);
                if (restSec != null) ex.setRestSec(restSec);
                if (!mediaTypeStr.isEmpty()) {
                    try { ex.setMediaType(com.homefit.server.domain.MediaType.valueOf(mediaTypeStr.toUpperCase())); } catch (Exception ignored) {}
                }
                if (!mediaUrl.isEmpty()) ex.setMediaUrl(mediaUrl);
                if (sortOrder != null) ex.setSortOrder(sortOrder);
                if (active != null) ex.setActive(active);

                exercises.save(ex);
                count++;
            }
        }
        return count;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try { return Long.parseLong(val.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return null; }
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
