package com.homefit.app.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

public class DateUtilsTest {

    @Test
    public void testIsoParsing() {
        assertEquals(0L, DateUtils.parseIso(null));
        assertEquals(0L, DateUtils.parseIso(""));
        assertEquals(0L, DateUtils.parseIso("invalid"));

        long expected = 1730044800000L; // 2024-10-27T16:00:00Z
        assertEquals(expected, DateUtils.parseIso("2024-10-27T16:00:00Z"));
        assertEquals(expected, DateUtils.parseIso("2024-10-27T16:00:00")); // Mất chữ Z
    }

    @Test
    public void testMonthStartAndEndMillis() {
        YearMonth month = YearMonth.of(2026, 11);
        long start = DateUtils.monthStartMillis(month);
        long end = DateUtils.monthEndMillis(month);

        LocalDate startDate = InstantToLocalDate(start);
        LocalDate endDate = InstantToLocalDate(end);

        assertEquals(LocalDate.of(2026, 11, 1), startDate);
        // Mili giây cuối cùng của tháng phải ở ngày 30
        assertEquals(LocalDate.of(2026, 11, 30), endDate);
    }
    
    private LocalDate InstantToLocalDate(long ms) {
        return java.time.Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
