package com.homefit.app.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.homefit.app.R;
import com.homefit.app.databinding.FragmentWorkoutSummaryBinding;

public class WorkoutSummaryFragment extends Fragment {

    private FragmentWorkoutSummaryBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int totalExercises = getArguments() != null ? getArguments().getInt("totalExercises", 1) : 1;
        int totalSeconds = getArguments() != null ? getArguments().getInt("totalSeconds", 0) : 0;
        int activeDurationSec = getArguments() != null ? getArguments().getInt("activeDurationSec", totalSeconds) : totalSeconds;
        double caloriesBurned = getArguments() != null ? getArguments().getDouble("caloriesBurned", 0.0) : 0.0;
        if (caloriesBurned <= 0.0 && activeDurationSec > 0) {
            caloriesBurned = (4.0 * 3.5 * 65.0 * (activeDurationSec / 60.0)) / 200.0;
        }

        String groupKey = getArguments() != null ? getArguments().getString("groupKey", "") : "";
        String difficulty = getArguments() != null ? getArguments().getString("difficulty", "") : "";
        String groupName = getArguments() != null ? getArguments().getString("groupName", "") : "";
        String customWorkoutId = getArguments() != null ? getArguments().getString("customWorkoutId", null) : null;

        // Lưu buổi tập vào CSDL Room & đồng bộ lên Server Render
        long now = System.currentTimeMillis();
        long startedAt = now - (Math.max(totalSeconds, 1) * 1000L);
        com.homefit.app.data.repository.WorkoutLogRepository logRepo =
                new com.homefit.app.data.repository.WorkoutLogRepository(requireContext());

        final double finalCalories = caloriesBurned;
        com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
            int previousCount = com.homefit.app.data.local.AppDatabase.getInstance(requireContext()).workoutLogDao().getCountNow();
            logRepo.saveWorkout(null, startedAt, now, totalSeconds, activeDurationSec, finalCalories, totalExercises);

            // Nâng cao Retention: Tự động bật nhắc tập 18:00 sau buổi đầu tiên nếu chưa bật
            if (previousCount == 0) {
                com.homefit.app.ui.settings.SettingsStore settingsStore = new com.homefit.app.ui.settings.SettingsStore(requireContext());
                if (!settingsStore.isReminderEnabled()) {
                    settingsStore.setReminderEnabled(true);
                    settingsStore.setReminderTime(18, 0);
                    com.homefit.app.notification.ReminderScheduler.scheduleDaily(requireContext(), 18, 0);
                    com.homefit.app.util.AppExecutors.mainThread().execute(() -> {
                        if (isAdded() && getContext() != null) {
                            android.widget.Toast.makeText(requireContext(),
                                    "Chúc mừng bạn đã hoàn thành buổi tập đầu tiên! 🎯 Đã bật nhắc nhở luyện tập lúc 18:00 hằng ngày.",
                                    android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        binding.tvCompletedText.setText(totalExercises + " bài tập đã được hoàn thành.");

        binding.btnBack.setOnClickListener(v -> exitSummary());
        binding.btnFinish.setOnClickListener(v -> exitSummary());
        binding.btnContinueWorkout.setOnClickListener(v -> exitSummary());

        binding.btnRestartWorkout.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle args = new Bundle();
            args.putString("groupKey", groupKey);
            args.putString("difficulty", difficulty);
            args.putString("groupName", groupName);
            if (customWorkoutId != null) {
                args.putString("customWorkoutId", customWorkoutId);
            }
            args.putInt("resumeIndex", 0);
            navController.navigate(R.id.action_player_to_summary, args);
            // Replace with restarting player
            navController.popBackStack(R.id.homeWorkoutFragment, false);
            navController.navigate(R.id.playerFragment, args);
        });
    }

    private void exitSummary() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack(R.id.homeWorkoutFragment, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
