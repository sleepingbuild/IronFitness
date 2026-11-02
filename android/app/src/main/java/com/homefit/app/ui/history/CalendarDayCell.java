package com.homefit.app.ui.history;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Dữ liệu một ô trên lưới Calendar tháng (42 ô = 6 hàng x 7 cột, bắt đầu từ Thứ Hai).
 * Ô đệm ngoài tháng có date == null.
 */
public final class CalendarDayCell {

    @Nullable
    public final LocalDate date;
    public final boolean inCurrentMonth;
    public final boolean isToday;
    public final boolean isSelected;
    public final boolean hasWorkout;

    public CalendarDayCell(@Nullable LocalDate date,
                           boolean inCurrentMonth,
                           boolean isToday,
                           boolean isSelected,
                           boolean hasWorkout) {
        this.date = date;
        this.inCurrentMonth = inCurrentMonth;
        this.isToday = isToday;
        this.isSelected = isSelected;
        this.hasWorkout = hasWorkout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalendarDayCell)) return false;
        CalendarDayCell that = (CalendarDayCell) o;
        return inCurrentMonth == that.inCurrentMonth
                && isToday == that.isToday
                && isSelected == that.isSelected
                && hasWorkout == that.hasWorkout
                && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, inCurrentMonth, isToday, isSelected, hasWorkout);
    }
}