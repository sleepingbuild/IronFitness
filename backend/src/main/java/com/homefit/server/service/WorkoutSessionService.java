package com.homefit.server.service;

import com.homefit.server.api.ApiException;
import com.homefit.server.api.dto.BatchSessionsResponse;
import com.homefit.server.api.dto.WorkoutSessionItem;
import com.homefit.server.api.dto.WorkoutSessionRequest;
import com.homefit.server.domain.WorkoutSession;
import com.homefit.server.repository.CategoryRepository;
import com.homefit.server.repository.WorkoutSessionRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Lưu và đọc lịch sử buổi tập. Idempotent theo (userId, clientUuid): gửi lại cùng clientUuid
 * trả bản cũ, không tạo bản trùng. userId luôn lấy từ token.
 * Mỗi buổi tập được lưu trong giao dịch riêng để một bản lỗi không làm hỏng cả lô.
 */
@Service
public class WorkoutSessionService {

    private static final Logger log = LoggerFactory.getLogger(WorkoutSessionService.class);

    /** Cho phép đồng hồ máy lệch tối đa 1 ngày so với máy chủ. */
    static final Duration MAX_CLOCK_SKEW = Duration.ofDays(1);

    public record SaveResult(WorkoutSession session, boolean created) {
    }

    private final WorkoutSessionRepository sessions;
    private final CategoryRepository categories;
    private final TransactionTemplate tx;
    private final Validator validator;

    public WorkoutSessionService(WorkoutSessionRepository sessions, CategoryRepository categories,
                                 TransactionTemplate tx, Validator validator) {
        this.sessions = sessions;
        this.categories = categories;
        this.tx = tx;
        this.validator = validator;
    }

    public SaveResult save(Long userId, WorkoutSessionRequest request) {
        checkBusinessRules(request, Instant.now());
        String uuid = normalizeUuid(request.clientUuid());
        try {
            return tx.execute(status -> doSave(userId, uuid, request));
        } catch (DataIntegrityViolationException e) {
            // Hai yêu cầu cùng clientUuid đến đồng thời: bản kia đã ghi, trả bản đó.
            SaveResult existing = tx.execute(status -> sessions.findByUserIdAndClientUuid(userId, uuid)
                    .map(s -> new SaveResult(s, false)).orElse(null));
            if (existing == null) {
                throw e;
            }
            return existing;
        }
    }

    public BatchSessionsResponse saveBatch(Long userId, List<WorkoutSessionRequest> items) {
        List<String> synced = new ArrayList<>();
        List<BatchSessionsResponse.Failure> failed = new ArrayList<>();
        for (WorkoutSessionRequest item : items) {
            String uuid = item == null ? null : item.clientUuid();
            if (item == null) {
                failed.add(new BatchSessionsResponse.Failure(null, "Phần tử rỗng"));
                continue;
            }
            Set<ConstraintViolation<WorkoutSessionRequest>> violations = validator.validate(item);
            if (!violations.isEmpty()) {
                failed.add(new BatchSessionsResponse.Failure(uuid,
                        violations.iterator().next().getMessage()));
                continue;
            }
            try {
                save(userId, item);
                synced.add(uuid);
            } catch (ApiException e) {
                failed.add(new BatchSessionsResponse.Failure(uuid, e.getMessage()));
            } catch (RuntimeException e) {
                log.warn("Không lưu được buổi tập {}: {}", uuid, e.getMessage());
                failed.add(new BatchSessionsResponse.Failure(uuid, "Lỗi máy chủ khi lưu buổi tập"));
            }
        }
        return new BatchSessionsResponse(synced, failed);
    }

    public List<WorkoutSessionItem> list(Long userId, Instant from, Instant to) {
        return tx.execute(status -> sessions
                .findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(userId, from, to)
                .stream().map(WorkoutSessionItem::from).toList());
    }

    private SaveResult doSave(Long userId, String uuid, WorkoutSessionRequest request) {
        var existing = sessions.findByUserIdAndClientUuid(userId, uuid);
        if (existing.isPresent()) {
            return new SaveResult(existing.get(), false);
        }
        if (request.categoryId() != null && !categories.existsById(request.categoryId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "categoryId không tồn tại");
        }
        WorkoutSession session = new WorkoutSession();
        session.setUserId(userId);
        session.setClientUuid(uuid);
        session.setCategoryId(request.categoryId());
        session.setStartedAt(request.startedAt());
        session.setCompletedAt(request.completedAt());
        session.setTotalSeconds(request.totalSeconds());
        session.setExercisesDone(request.exercisesDone());
        return new SaveResult(sessions.saveAndFlush(session), true);
    }

    /** Quy tắc liên trường mà chú thích kiểm tra dữ liệu không diễn đạt được. Tách ra để kiểm thử không cần CSDL. */
    static void checkBusinessRules(WorkoutSessionRequest request, Instant now) {
        if (request.completedAt().isBefore(request.startedAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "completedAt không được sớm hơn startedAt");
        }
        if (request.completedAt().isAfter(now.plus(MAX_CLOCK_SKEW))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "completedAt nằm trong tương lai");
        }
    }

    static String normalizeUuid(String clientUuid) {
        return clientUuid.trim().toLowerCase(Locale.ROOT);
    }
}
