package com.homefit.app.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.homefit.app.databinding.FragmentHistoryBinding;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        // Gửi yêu cầu sync (nếu có mạng sẽ đẩy các session pending lên server)
        viewModel.syncHistory();

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getTotalWorkouts().observe(getViewLifecycleOwner(), count -> {
            binding.tvTotalWorkouts.setText(count != null ? String.valueOf(count) : "0");
        });

        viewModel.getTotalTimeSeconds().observe(getViewLifecycleOwner(), totalSecs -> {
            if (totalSecs != null) {
                int minutes = totalSecs / 60;
                binding.tvTotalMinutes.setText(String.valueOf(minutes));
            } else {
                binding.tvTotalMinutes.setText("0");
            }
        });

        // Tạm thời chưa tuỳ chỉnh được giao diện chấm tròn của CalendarView mặc định của Android
        // nên ta chỉ lấy dữ liệu về để sẵn.
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        viewModel.getWorkoutDates(thirtyDaysAgo, now).observe(getViewLifecycleOwner(), dates -> {
            // Khi có custom view, ta sẽ truyền list ngày này vào.
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}