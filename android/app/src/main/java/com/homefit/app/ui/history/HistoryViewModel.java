package com.homefit.app.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.data.local.entity.WorkoutLogEntity;
import com.homefit.app.data.repository.ExerciseRepository;
import com.homefit.app.data.repository.WorkoutLogRepository;
import com.homefit.app.data.remote.dto.StatsResponse;
import com.homefit.app.data.repository.AuthRepository;
import com.homefit.app.util.DateUtils;
import com.homefit.app.util.Resource;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel cho màn Lịch sử & Thống kê (W2-TV6-06 / W2-TV2-03,04).
 * Giữ tháng đang xem và ngày đang chọn qua xoay màn hình, dựng lưới 42 ô cho Calendar.
 */
public class HistoryViewModel extends AndroidViewModel {

    private final WorkoutLogRepository workoutLogRepository;
    private final ExerciseRepository exerciseRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<YearMonth> currentMonth = new MutableLiveData<>(YearMonth.now());
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    
    private final LiveData<Resource<StatsResponse>> apiStats;

    private final LiveData<List<WorkoutLogEntity>> monthLogs;
    private final LiveData<List<Long>> monthWorkoutTimestamps;
    private final MediatorLiveData<List<CalendarDayCell>> calendarCells = new MediatorLiveData<>();
    private final MediatorLiveData<List<WorkoutLogEntity>> selectedDaySessions = new MediatorLiveData<>();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        this.workoutLogRepository = new WorkoutLogRepository(application);
        this.exerciseRepository = new ExerciseRepository(application);
        this.authRepository = new AuthRepository(application);

        this.apiStats = authRepository.getStats();

        this.monthLogs = Transformations.switchMap(currentMonth, month ->
                workoutLogRepository.getByMonth(
                        DateUtils.monthStartMillis(month),
                        DateUtils.monthEndMillis(month)));

        this.monthWorkoutTimestamps = Transformations.switchMap(currentMonth, month ->
                workoutLogRepository.getDatesWithWorkout(
                        DateUtils.monthStartMillis(month),
                        DateUtils.monthEndMillis(month)));

        calendarCells.addSource(currentMonth, m -> rebuildCalendar());
        calendarCells.addSource(selectedDate, d -> rebuildCalendar());
        calendarCells.addSource(monthWorkoutTimestamps, ts -> rebuildCalendar());

        selectedDaySessions.addSource(selectedDate, d -> filterSessionsForDay());
        selectedDaySessions.addSource(monthLogs, logs -> filterSessionsForDay());

        workoutLogRepository.syncPending();
    }

    public LiveData<YearMonth> getCurrentMonth() {
        return currentMonth;
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<CalendarDayCell>> getCalendarCells() {
        return calendarCells;
    }

    public LiveData<List<WorkoutLogEntity>> getSelectedDaySessions() {
        return selectedDaySessions;
    }

    public LiveData<Integer> getTotalCount() {
        return workoutLogRepository.getTotalCount();
    }

    public LiveData<Integer> getTotalSeconds() {
        return workoutLogRepository.getTotalSeconds();
    }

    public LiveData<Resource<StatsResponse>> getApiStats() {
        return apiStats;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return exerciseRepository.getCategories();
    }

    public void previousMonth() {
        YearMonth cur = currentMonth.getValue();
        if (cur != null) {
            YearMonth prev = cur.minusMonths(1);
            currentMonth.setValue(prev);
            adjustSelectedDateForMonth(prev);
        }
    }

    public void nextMonth() {
        YearMonth cur = currentMonth.getValue();
        if (cur != null) {
            YearMonth next = cur.plusMonths(1);
            currentMonth.setValue(next);
            adjustSelectedDateForMonth(next);
        }
    }

    public void goToToday() {
        LocalDate today = LocalDate.now();
        currentMonth.setValue(YearMonth.from(today));
        selectedDate.setValue(today);
    }

    public void selectDate(@NonNull LocalDate date) {
        selectedDate.setValue(date);
        YearMonth targetMonth = YearMonth.from(date);
        if (!targetMonth.equals(currentMonth.getValue())) {
            currentMonth.setValue(targetMonth);
        }
    }

    public void syncNow() {
        workoutLogRepository.syncPending();
        workoutLogRepository.restoreHistory();
    }

    private void adjustSelectedDateForMonth(@NonNull YearMonth month) {
        LocalDate today = LocalDate.now();
        if (YearMonth.from(today).equals(month)) {
            selectedDate.setValue(today);
        } else {
            selectedDate.setValue(month.atDay(1));
        }
    }

    private void rebuildCalendar() {
        YearMonth month = currentMonth.getValue();
        if (month == null) {
            return;
        }
        LocalDate selected = selectedDate.getValue();
        LocalDate today = LocalDate.now();
        List<Long> timestamps = monthWorkoutTimestamps.getValue();

        Set<LocalDate> workoutDates = new HashSet<>();
        if (timestamps != null) {
            for (Long ms : timestamps) {
                if (ms != null) {
                    workoutDates.add(DateUtils.toLocalDate(ms));
                }
            }
        }

        LocalDate firstOfMonth = month.atDay(1);
        // DayOfWeek: MONDAY = 1 ... SUNDAY = 7 -> số ô trống đầu tháng = getValue() - 1
        int leadingEmpty = firstOfMonth.getDayOfWeek().getValue() - 1;
        int daysInMonth = month.lengthOfMonth();
        int totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7;

        List<CalendarDayCell> cells = new ArrayList<>(totalCells);
        for (int i = 0; i < leadingEmpty; i++) {
            cells.add(new CalendarDayCell(null, false, false, false, false));
        }
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selected);
            boolean hasWorkout = workoutDates.contains(date);
            cells.add(new CalendarDayCell(date, true, isToday, isSelected, hasWorkout));
        }
        while (cells.size() < totalCells) {
            cells.add(new CalendarDayCell(null, false, false, false, false));
        }
        calendarCells.setValue(cells);
    }

    private void filterSessionsForDay() {
        LocalDate selected = selectedDate.getValue();
        List<WorkoutLogEntity> logs = monthLogs.getValue();
        if (selected == null || logs == null || logs.isEmpty()) {
            selectedDaySessions.setValue(Collections.emptyList());
            return;
        }
        List<WorkoutLogEntity> filtered = new ArrayList<>();
        for (WorkoutLogEntity log : logs) {
            if (selected.equals(DateUtils.toLocalDate(log.completedAt))) {
                filtered.add(log);
            }
        }
        selectedDaySessions.setValue(filtered);
    }
}