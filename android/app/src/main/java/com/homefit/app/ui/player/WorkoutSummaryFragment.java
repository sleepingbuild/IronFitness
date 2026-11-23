package com.homefit.app.ui.player;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.databinding.FragmentWorkoutSummaryBinding;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.StreakCalculator;

import java.util.List;
import java.util.Locale;

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

        // Render chỉ số
        binding.tvCompletedText.setText(totalExercises + " bài tập đã được hoàn thành.");
        binding.tvSummaryCalories.setText(String.format(Locale.US, "%.0f kcal", caloriesBurned));
        binding.tvSummaryDuration.setText(formatDuration(activeDurationSec));

        // Lưu buổi tập vào CSDL Room & đồng bộ lên Server Render
        long now = System.currentTimeMillis();
        long startedAt = now - (Math.max(totalSeconds, 1) * 1000L);
        com.homefit.app.data.repository.WorkoutLogRepository logRepo =
                new com.homefit.app.data.repository.WorkoutLogRepository(requireContext());

        final double finalCalories = caloriesBurned;
        AppExecutors.diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int previousCount = db.workoutLogDao().getCountNow();
            logRepo.saveWorkout(null, startedAt, now, totalSeconds, activeDurationSec, finalCalories, totalExercises);

            List<Long> completedTimes = db.workoutLogDao().getAllCompletedTimesNow();
            StreakCalculator.Result streakResult = StreakCalculator.calculateStreak(completedTimes);

            AppExecutors.mainThread().execute(() -> {
                if (binding != null) {
                    binding.tvSummaryStreak.setText(streakResult.streakCount + " ngày 🔥");
                }

                // Retention Booster: Hộp thoại gợi ý bật nhắc tập 18:00 hằng ngày sau buổi tập đầu tiên
                if (previousCount == 0 && isAdded() && getContext() != null) {
                    showFirstWorkoutReminderDialog();
                }
            });
        });

        binding.btnLogWeightSummary.setOnClickListener(v -> showWeightInputDialog());

        com.homefit.app.ui.settings.SettingsStore settingsStore = new com.homefit.app.ui.settings.SettingsStore(requireContext());
        updateFeedbackButtonsUI(settingsStore.getDifficultyMultiplier());

        binding.btnFeedbackTooEasy.setOnClickListener(v -> {
            settingsStore.setDifficultyMultiplier(1.15f);
            updateFeedbackButtonsUI(1.15f);
            Toast.makeText(requireContext(), "Đã ghi nhận Quá dễ! Tăng 15% khối lượng ở các buổi sau 🚀", Toast.LENGTH_SHORT).show();
        });

        binding.btnFeedbackJustRight.setOnClickListener(v -> {
            settingsStore.setDifficultyMultiplier(1.0f);
            updateFeedbackButtonsUI(1.0f);
            Toast.makeText(requireContext(), "Đã ghi nhận Vừa sức! Giữ nguyên tiến trình tập 👍", Toast.LENGTH_SHORT).show();
        });

        binding.btnFeedbackTooHard.setOnClickListener(v -> {
            settingsStore.setDifficultyMultiplier(0.85f);
            updateFeedbackButtonsUI(0.85f);
            Toast.makeText(requireContext(), "Đã ghi nhận Quá nặng! Giảm 15% khối lượng ở các buổi sau 🥵", Toast.LENGTH_SHORT).show();
        });

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
            navController.popBackStack(R.id.homeWorkoutFragment, false);
            navController.navigate(R.id.playerFragment, args);
        });
    }

    private String formatDuration(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        if (min > 0) {
            return String.format(Locale.US, "%d:%02d", min, sec);
        }
        return seconds + "s";
    }

    private void showWeightInputDialog() {
        final EditText etWeight = new EditText(requireContext());
        etWeight.setHint("Ví dụ: 65.0");
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(requireContext())
                .setTitle("Ghi nhận cân nặng hiện tại (kg)")
                .setMessage("Nhập số cân nặng để theo dõi biểu đồ trên tab Báo cáo:")
                .setView(etWeight)
                .setPositiveButton("Lưu cân nặng", (dialog, which) -> {
                    try {
                        String input = etWeight.getText().toString().trim();
                        if (!input.isEmpty()) {
                            double w = Double.parseDouble(input);
                            if (w > 20 && w < 300) {
                                saveWeightToDb(w);
                            }
                        }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveWeightToDb(double weight) {
        AppExecutors.diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserProfileEntity userProfile = db.userProfileDao().getNow();
            if (userProfile == null) userProfile = new UserProfileEntity();
            userProfile.weightKg = weight;
            db.userProfileDao().upsert(userProfile);
            AppExecutors.mainThread().execute(() -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(), "Đã lưu cân nặng thành công! ⚖️", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showFirstWorkoutReminderDialog() {
        com.homefit.app.ui.settings.SettingsStore settingsStore = new com.homefit.app.ui.settings.SettingsStore(requireContext());
        if (settingsStore.isReminderEnabled()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("⏰ Bật nhắc nhở tập luyện hằng ngày?")
                .setMessage("Chúc mừng bạn đã hoàn thành buổi tập đầu tiên! 🎯 Bạn có muốn bật nhắc nhở lúc 18:00 hằng ngày để xây dựng thói quen tốt không?")
                .setPositiveButton("Bật nhắc nhở 18:00", (dialog, which) -> {
                    settingsStore.setReminderEnabled(true);
                    settingsStore.setReminderTime(18, 0);
                    com.homefit.app.notification.ReminderScheduler.scheduleDaily(requireContext(), 18, 0);
                    Toast.makeText(requireContext(), "Đã bật nhắc nhở lúc 18:00 hằng ngày! 🔔", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Để sau", null)
                .show();
    }

    private void updateFeedbackButtonsUI(float multiplier) {
        if (binding == null) return;
        boolean isEasy = (multiplier > 1.05f);
        boolean isHard = (multiplier < 0.95f);
        boolean isRight = (!isEasy && !isHard);

        int activeColor = requireContext().getColor(R.color.hf_primary);

        binding.btnFeedbackTooEasy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isEasy ? activeColor : android.graphics.Color.TRANSPARENT));
        binding.btnFeedbackTooEasy.setTextColor(isEasy ? requireContext().getColor(R.color.white) : activeColor);

        binding.btnFeedbackJustRight.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isRight ? activeColor : android.graphics.Color.TRANSPARENT));
        binding.btnFeedbackJustRight.setTextColor(isRight ? requireContext().getColor(R.color.white) : activeColor);

        binding.btnFeedbackTooHard.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isHard ? activeColor : android.graphics.Color.TRANSPARENT));
        binding.btnFeedbackTooHard.setTextColor(isHard ? requireContext().getColor(R.color.white) : activeColor);
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
