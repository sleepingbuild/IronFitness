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

    /** Parse ISO-8601 hoặc Epoch millis dạng chuỗi; chấp nhận cả chuỗi thiếu "Z". Lỗi parse trả về 0. */
    public static long parseIso(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return 0L;
        }
        String s = iso.trim();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {}

        try {
            return Instant.parse(s).toEpochMilli();
        } catch (DateTimeParseException first) {
            try {
                return Instant.parse(s + "Z").toEpochMilli();
            } catch (DateTimeParseException second) {
                try {
                    return java.time.LocalDateTime.parse(s).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                } catch (Exception third) {
                    return 0L;
                }
            }
        }
    }

    /** Format epoch millis thành định dạng dd/MM/yyyy */
    @NonNull
    public static String formatDateDdMmYyyy(long epochMillis) {
        if (epochMillis <= 0) return "";
        try {
            java.time.LocalDate date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
            return String.format(java.util.Locale.US, "%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
        } catch (Exception e) {
            return "";
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
