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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Quản lý bài tập trên trang quản trị. Xóa là xóa mềm (ẩn) để không làm hỏng lịch sử. */
@Service
public class AdminExerciseService {

    public record Row(Long id, String categoryName, String name, String modeText, int restSec,
                      String mediaType, boolean active, String updatedAtText) {
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
    public List<Row> listRows(Long categoryId) {
        Map<Long, String> names = categories.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        List<Exercise> list = categoryId == null
                ? exercises.findAllByOrderByCategoryIdAscSortOrderAscIdAsc()
                : exercises.findByCategoryIdOrderBySortOrderAscIdAsc(categoryId);
        return list.stream().map(e -> new Row(e.getId(),
                names.getOrDefault(e.getCategoryId(), "?"), e.getName(), modeText(e), e.getRestSec(),
                e.getMediaType().name(), e.isActive(),
                e.getUpdatedAt() == null ? "" : DISPLAY.format(e.getUpdatedAt()))).toList();
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

    /** Ẩn hoặc hiện lại. updatedAt đổi nên app xóa hoặc thêm bài ở lần đồng bộ sau. */
    @Transactional
    public void toggle(Long id) {
        Exercise exercise = find(id);
        exercise.setActive(!exercise.isActive());
        exercises.save(exercise);
    }

    private Exercise find(Long id) {
        return exercises.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));
    }

    private static String modeText(Exercise e) {
        return e.getDurationSec() != null ? e.getDurationSec() + " giây" : e.getReps() + " lần";
    }

    /** Điền đúng một trong durationSec hoặc reps, đúng ràng buộc CHECK của MySQL. */
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
