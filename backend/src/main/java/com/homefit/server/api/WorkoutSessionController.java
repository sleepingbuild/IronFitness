package com.homefit.server.api;

import com.homefit.server.api.dto.BatchSessionsRequest;
import com.homefit.server.api.dto.BatchSessionsResponse;
import com.homefit.server.api.dto.WorkoutSessionItem;
import com.homefit.server.api.dto.WorkoutSessionRequest;
import com.homefit.server.api.dto.WorkoutSessionResponse;
import com.homefit.server.service.WorkoutSessionService;

import jakarta.validation.Valid;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/workout-sessions. userId luôn lấy từ token (claim sub), không nhận từ client. */
@RestController
@RequestMapping("/api/v1/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService service;

    public WorkoutSessionController(WorkoutSessionService service) {
        this.service = service;
    }

    /** 201 nếu buổi tập mới, 200 nếu clientUuid đã có (trả bản cũ, không tạo trùng). */
    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                         @Valid @RequestBody WorkoutSessionRequest request) {
        WorkoutSessionService.SaveResult result = service.save(userId(jwt), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(
                new WorkoutSessionResponse(result.session().getId(), result.session().getClientUuid()));
    }

    @PostMapping("/batch")
    public BatchSessionsResponse batch(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody BatchSessionsRequest request) {
        return service.saveBatch(userId(jwt), request.sessions());
    }

    /** Lọc theo thời điểm hoàn thành. Bỏ from/to thì lấy toàn bộ lịch sử. */
    @GetMapping
    public List<WorkoutSessionItem> list(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to) {
        Instant fromInstant = parse(from, "from", Instant.EPOCH);
        Instant toInstant = parse(to, "to", Instant.now().plus(Duration.ofDays(2)));
        return service.list(userId(jwt), fromInstant, toInstant);
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private static Instant parse(String value, String name, Instant defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    name + " phải theo định dạng ISO-8601, ví dụ 2026-10-24T08:00:00Z");
        }
    }
}
