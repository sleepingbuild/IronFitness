package com.homefit.app.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.databinding.FragmentHistoryBinding;
import com.homefit.app.ui.common.SnackbarHelper;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Màn Lịch sử & Thống kê (W2-TV6-06 + W2-TV6-07 Banner AdMob).
 */
public class HistoryFragment extends Fragment {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Nullable
    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private CalendarAdapter calendarAdapter;
    private WorkoutLogAdapter logAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) {
            return;
        }

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        calendarAdapter = new CalendarAdapter(date -> viewModel.selectDate(date));
        binding.rvCalendarDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendarDays.setItemAnimator(null);
        binding.rvCalendarDays.setAdapter(calendarAdapter);

        logAdapter = new WorkoutLogAdapter();
        binding.rvDaySessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDaySessions.setAdapter(logAdapter);

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
        binding.btnToday.setOnClickListener(v -> viewModel.goToToday());
        binding.btnSyncHistory.setOnClickListener(v -> {
            viewModel.syncNow();
            if (binding != null) {
                SnackbarHelper.show(this, getString(R.string.history_sync_started));
            }
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getTotalCount().observe(getViewLifecycleOwner(), count -> {
            if (binding != null) {
                int safeCount = count != null ? count : 0;
                binding.tvTotalSessionsValue.setText(getString(R.string.stats_sessions_format, safeCount));
            }
        });

        viewModel.getTotalSeconds().observe(getViewLifecycleOwner(), seconds -> {
            if (binding != null) {
                int totalMinutes = (seconds != null ? seconds : 0) / 60;
                binding.tvTotalMinutesValue.setText(getString(R.string.stats_minutes_format, totalMinutes));
            }
        });

        viewModel.getApiStats().observe(getViewLifecycleOwner(), resource -> {
            if (binding != null && resource.isSuccess() && resource.data != null) {
                binding.tvCurrentStreakValue.setText(getString(R.string.stats_streak_format, resource.data.currentStreakDays));
                binding.tvLast7DaysValue.setText(String.valueOf(resource.data.sessionsLast7Days));
                binding.tvLast30DaysValue.setText(String.valueOf(resource.data.sessionsLast30Days));
                
                int t = resource.data.totalSessions;
                String badge;
                if (t >= 30) badge = "🏅 Tinh anh";
                else if (t >= 10) badge = "🥇 Chiến binh";
                else if (t >= 5) badge = "🥈 Người quen";
                else badge = "🏆 Tân binh";
                
                binding.tvBadgeValue.setText(badge);
            }
        });

        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), month -> {
            if (binding != null && month != null) {
                binding.tvMonthYear.setText(getString(
                        R.string.calendar_month_year_format,
                        month.getMonthValue(),
                        month.getYear()));
            }
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (binding != null && date != null) {
                binding.tvSelectedDateHeader.setText(getString(
                        R.string.history_selected_date_format,
                        date.format(DATE_FORMATTER)));
            }
        });

        viewModel.getCalendarCells().observe(getViewLifecycleOwner(), cells ->
                calendarAdapter.submitList(cells));

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Map<Long, String> map = new HashMap<>();
                for (CategoryEntity c : categories) {
                    map.put(c.id, c.name);
                }
                logAdapter.setCategoryNames(map);
            }
        });

        viewModel.getSelectedDaySessions().observe(getViewLifecycleOwner(), sessions -> {
            if (binding == null) {
                return;
            }
            boolean empty = sessions == null || sessions.isEmpty();
            binding.tvEmptyDaySessions.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvDaySessions.setVisibility(empty ? View.GONE : View.VISIBLE);
            logAdapter.submitList(sessions);
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}