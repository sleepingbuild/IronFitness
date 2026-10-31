package com.homefit.server.service;

import com.homefit.server.api.dto.CategoryResponse;
import com.homefit.server.api.dto.ExerciseResponse;
import com.homefit.server.domain.Category;
import com.homefit.server.domain.Exercise;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.ExerciseRepository;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đọc thư viện bài tập cho app.
 * Không có updatedAfter: chỉ trả bản ghi đang hoạt động.
 * Có updatedAfter: trả mọi bản ghi đổi sau mốc đó, kể cả bản vừa bị tắt (isActive = false).
 */
@Service
public class LibraryService {

    private final CategoryRepository categories;
    private final ExerciseRepository exercises;

    public LibraryService(CategoryRepository categories, ExerciseRepository exercises) {
        this.categories = categories;
        this.exercises = exercises;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Instant updatedAfter) {
        List<Category> list = updatedAfter == null
                ? categories.findByActiveTrueOrderBySortOrderAscIdAsc()
                : categories.findByUpdatedAtAfterOrderBySortOrderAscIdAsc(updatedAfter);
        return list.stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercises(Long categoryId, Instant updatedAfter) {
        List<Exercise> list;
        if (updatedAfter == null) {
            list = categoryId == null
                    ? exercises.findByActiveTrueOrderBySortOrderAscIdAsc()
                    : exercises.findByCategoryIdAndActiveTrueOrderBySortOrderAscIdAsc(categoryId);
        } else {
            list = categoryId == null
                    ? exercises.findByUpdatedAtAfterOrderBySortOrderAscIdAsc(updatedAfter)
                    : exercises.findByCategoryIdAndUpdatedAtAfterOrderBySortOrderAscIdAsc(categoryId, updatedAfter);
        }
        return list.stream().map(ExerciseResponse::from).toList();
    }
}
