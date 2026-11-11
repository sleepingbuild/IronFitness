package com.homefit.app.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StreakCalculator {

    public static class Result {
        public int streakCount;
        public boolean isFlameActive;

        public Result(int streakCount, boolean isFlameActive) {
            this.streakCount = streakCount;
            this.isFlameActive = isFlameActive;
        }
    }

    public static Result calculateStreak(List<Long> completedTimestamps) {
        if (completedTimestamps == null || completedTimestamps.isEmpty()) {
            return new Result(0, false);
        }

        // Chuyển timestamp thành danh sách các ngày (epochDay) duy nhất
        Set<Long> uniqueDaysSet = new HashSet<>();
        for (Long ts : completedTimestamps) {
            if (ts != null && ts > 0) {
                long epochDay = toEpochDay(ts);
                uniqueDaysSet.add(epochDay);
            }
        }

        if (uniqueDaysSet.isEmpty()) {
            return new Result(0, false);
        }

        List<Long> sortedDays = new ArrayList<>(uniqueDaysSet);
        Collections.sort(sortedDays, Collections.reverseOrder()); // Ngày mới nhất ở đầu

        long todayEpochDay = toEpochDay(System.currentTimeMillis());
        long latestWorkoutDay = sortedDays.get(0);

        long gapFromToday = todayEpochDay - latestWorkoutDay;

        // Cho phép tối đa nghỉ 1 ngày (gapFromToday <= 2)
        // gap = 0: tập hôm nay
        // gap = 1: tập hôm qua
        // gap = 2: nghỉ hôm qua (cho phép 1 ngày nghỉ)
        // gap > 2: quá 1 ngày không tập -> mất lửa!
        boolean isFlameActive = (gapFromToday <= 2);

        if (!isFlameActive) {
            return new Result(0, false);
        }

        // Tính chuỗi tập liên tiếp (cho phép khoảng cách tối đa 2 ngày giữa các lần tập = 1 ngày nghỉ)
        int streak = 1;
        for (int i = 0; i < sortedDays.size() - 1; i++) {
            long current = sortedDays.get(i);
            long next = sortedDays.get(i + 1);
            long diff = current - next;

            if (diff <= 2) { // liên tiếp hoặc cách 1 ngày nghỉ
                streak++;
            } else {
                break;
            }
        }

        return new Result(streak, true);
    }

    private static long toEpochDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / (24 * 60 * 60 * 1000L);
    }
}
