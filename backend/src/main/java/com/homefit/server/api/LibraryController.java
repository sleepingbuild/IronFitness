package com.homefit.server.api;

import com.homefit.server.api.dto.CategoryResponse;
import com.homefit.server.api.dto.ExerciseResponse;
import com.homefit.server.service.LibraryService;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/categories và /api/v1/exercises (người dùng đã đăng nhập được đọc). */
@RestController
@RequestMapping("/api/v1")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories(@RequestParam(required = false) String updatedAfter) {
        return libraryService.getCategories(parseInstant(updatedAfter));
    }

    @GetMapping("/exercises")
    public List<ExerciseResponse> exercises(@RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) String updatedAfter) {
        return libraryService.getExercises(categoryId, parseInstant(updatedAfter));
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "updatedAfter phải theo định dạng ISO-8601, ví dụ 2026-10-24T08:00:00Z");
        }
    }
}
