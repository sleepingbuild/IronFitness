package com.homefit.app.util;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Chuyển đổi thời gian. Server dùng ISO-8601 UTC ("2026-10-24T08:30:00Z"), Room lưu epoch millis.
 * Ngày trên Calendar tính theo múi giờ máy.
 */
public final class DateUtils {

    private DateUtils() {
    }

    /** Parse ISO-8601; chấp nhận cả chuỗi thiếu "Z". Lỗi parse trả về 0. */
    public static long parseIso(String iso) {
        if (iso == null || iso.isEmpty()) {
            return 0L;
        }
        try {
            return Instant.parse(iso).toEpochMilli();
        } catch (DateTimeParseException first) {
            try {
                return Instant.parse(iso + "Z").toEpochMilli();
            } catch (DateTimeParseException second) {
                return 0L;
            }
        }
    }

    /** Epoch millis sang ISO-8601 UTC, bỏ phần lẻ giây. */
    @NonNull
    public static String toIso(long epochMillis) {
        return Instant.ofEpochSecond(epochMillis / 1000L).toString();
    }

    /** Ngày (theo múi giờ máy) của một thời điểm. */
    @NonNull
    public static LocalDate toLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Mili giây đầu tiên của tháng (theo múi giờ máy). */
    public static long monthStartMillis(@NonNull YearMonth month) {
        return month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** Mili giây cuối cùng của tháng (theo múi giờ máy). */
    public static long monthEndMillis(@NonNull YearMonth month) {
        return month.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - 1L;
    }
}
