package com.homefit.app.ui.report;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.prefs.OnboardingStore;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.BmiCalculator;
import com.homefit.app.util.StreakCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private TextView tvWorkoutCount;
    private TextView tvKcalCount;
    private TextView tvMinutesCount;
    private TextView tvCurrentWeight;
    private TextView tvTargetWeightText;
    private TextView tvWeightProgressSubtitle;
    private LinearProgressIndicator pbWeightProgress;
    private TextView tvBmiValue;
    private TextView tvHeightValue;
    private TextView tvBarChartHeader;

    private RecyclerView rvBadges;
    private BadgeAdapter badgeAdapter;

    private UserProfileEntity userProfile;
    private OnboardingStore onboardingStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onboardingStore = new OnboardingStore(requireContext());

        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvKcalCount = view.findViewById(R.id.tvKcalCount);
        tvMinutesCount = view.findViewById(R.id.tvMinutesCount);
        tvCurrentWeight = view.findViewById(R.id.tvCurrentWeight);
        tvTargetWeightText = view.findViewById(R.id.tvTargetWeightText);
        tvWeightProgressSubtitle = view.findViewById(R.id.tvWeightProgressSubtitle);
        pbWeightProgress = view.findViewById(R.id.pbWeightProgress);
        tvBmiValue = view.findViewById(R.id.tvBmiValue);
        tvHeightValue = view.findViewById(R.id.tvHeightValue);
        tvBarChartHeader = view.findViewById(R.id.tvBarChartHeader);

        MaterialButton btnLogWeight = view.findViewById(R.id.btnLogWeight);
        MaterialButton btnEditBmi = view.findViewById(R.id.btnEditBmi);

        if (btnLogWeight != null) btnLogWeight.setOnClickListener(v -> showWeightDialog());
        if (btnEditBmi != null) btnEditBmi.setOnClickListener(v -> showHeightDialog());
        if (tvHeightValue != null) tvHeightValue.setOnClickListener(v -> showHeightDialog());

        rvBadges = view.findViewById(R.id.rvBadges);
        if (rvBadges != null) {
            badgeAdapter = new BadgeAdapter();
            rvBadges.setAdapter(badgeAdapter);
        }

        loadUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            userProfile = db.userProfileDao().getNow();

            final int count = db.workoutLogDao().getCountNow();
            int dbActiveSec = db.workoutLogDao().getTotalActiveSecondsNow();
            if (dbActiveSec <= 0) {
                dbActiveSec = db.workoutLogDao().getTotalSecondsNow();
            }
            double dbCalories = db.workoutLogDao().getTotalCaloriesNow();
            if (dbCalories <= 0.0 && dbActiveSec > 0) {
                double weight = (userProfile != null && userProfile.weightKg != null && userProfile.weightKg > 0)
                        ? userProfile.weightKg : 65.0;
                dbCalories = (4.0 * 3.5 * weight * (dbActiveSec / 60.0)) / 200.0;
            }

            List<Long> completedTimes = db.workoutLogDao().getAllCompletedTimesNow();
            StreakCalculator.Result streakResult = StreakCalculator.calculateStreak(completedTimes);

            final int finalActiveSec = dbActiveSec;
            final double finalCalories = dbCalories;

            AppExecutors.mainThread().execute(() -> {
                updateUi(count, finalActiveSec, finalCalories, streakResult);
            });
        });
    }

    private void updateUi(int workoutCount, int totalActiveSec, double totalCalories, StreakCalculator.Result streakResult) {
        if (!isAdded()) return;

        int totalMinutes = (totalActiveSec > 0) ? (int) Math.ceil(totalActiveSec / 60.0) : 0;
        int totalKcal = (int) Math.round(totalCalories);

        if (tvWorkoutCount != null) tvWorkoutCount.setText(String.valueOf(workoutCount));
        if (tvKcalCount != null) tvKcalCount.setText(String.valueOf(totalKcal));
        if (tvMinutesCount != null) tvMinutesCount.setText(String.valueOf(totalMinutes));

        if (tvBarChartHeader != null) {
            tvBarChartHeader.setText("Tổng: " + totalMinutes + " phút tập luyện");
        }

        float currentW = onboardingStore.getWeightKg();
        if (userProfile != null && userProfile.weightKg != null && userProfile.weightKg > 0) {
            currentW = userProfile.weightKg.floatValue();
        }
        float targetW = onboardingStore.getTargetWeightKg();

        if (tvCurrentWeight != null) tvCurrentWeight.setText(String.format(Locale.US, "%.1f kg", currentW));
        if (tvTargetWeightText != null) tvTargetWeightText.setText(String.format(Locale.US, "%.1f kg", targetW));

        float diff = Math.abs(currentW - targetW);
        if (tvWeightProgressSubtitle != null) {
            if (diff < 0.2f) {
                tvWeightProgressSubtitle.setText(R.string.weight_goal_reached);
            } else {
                tvWeightProgressSubtitle.setText(getString(R.string.weight_remaining_format, diff));
            }
        }
        if (pbWeightProgress != null) {
            int percent = (currentW > 0 && targetW > 0) ? Math.min(100, Math.max(10, (int) Math.round((targetW / currentW) * 100))) : 50;
            pbWeightProgress.setProgress(percent);
        }

        float heightCm = onboardingStore.getHeightCm();
        if (userProfile != null && userProfile.heightCm != null && userProfile.heightCm > 0) {
            heightCm = userProfile.heightCm.floatValue();
        }

        if (tvHeightValue != null) {
            tvHeightValue.setText(getString(R.string.height_format, Math.round(heightCm)));
        }

        if (heightCm > 0 && currentW > 0) {
            double bmi = BmiCalculator.calculate(heightCm, currentW);
            if (tvBmiValue != null) tvBmiValue.setText(String.format(Locale.US, "%.1f", bmi));
        }

        // Badges setup
        if (badgeAdapter != null) {
            List<BadgeAdapter.BadgeItem> badges = new ArrayList<>();
            badges.add(new BadgeAdapter.BadgeItem("🏆", getString(R.string.badge_1_title), getString(R.string.badge_1_desc), workoutCount >= 3));
            badges.add(new BadgeAdapter.BadgeItem("🔥", getString(R.string.badge_2_title), getString(R.string.badge_2_desc), streakResult.streakCount >= 7));
            badges.add(new BadgeAdapter.BadgeItem("⚡", getString(R.string.badge_3_title), getString(R.string.badge_3_desc), totalKcal >= 1000));
            badges.add(new BadgeAdapter.BadgeItem("👑", getString(R.string.badge_4_title), getString(R.string.badge_4_desc), workoutCount >= 24));
            badges.add(new BadgeAdapter.BadgeItem("🥇", getString(R.string.badge_5_title), getString(R.string.badge_5_desc), workoutCount >= 10));
            badges.add(new BadgeAdapter.BadgeItem("🎯", getString(R.string.badge_6_title), getString(R.string.badge_6_desc), workoutCount >= 5));
            badgeAdapter.submitList(badges);
        }
    }

    private void showWeightDialog() {
        final EditText etWeight = new EditText(requireContext());
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        float currentW = (userProfile != null && userProfile.weightKg != null) ? userProfile.weightKg.floatValue() : onboardingStore.getWeightKg();
        etWeight.setText(String.valueOf(currentW));

        new AlertDialog.Builder(requireContext())
                .setTitle("Ghi nhận cân nặng hiện tại (kg)")
                .setView(etWeight)
                .setPositiveButton("Lưu cân nặng", (dialog, which) -> {
                    try {
                        double w = Double.parseDouble(etWeight.getText().toString().trim());
                        if (w > 20 && w < 300) {
                            saveWeight(w);
                        }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showHeightDialog() {
        final EditText etHeight = new EditText(requireContext());
        etHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        float currentH = (userProfile != null && userProfile.heightCm != null) ? userProfile.heightCm.floatValue() : onboardingStore.getHeightCm();
        etHeight.setText(String.valueOf(currentH));

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa chiều cao (cm)")
                .setView(etHeight)
                .setPositiveButton("Lưu chiều cao", (dialog, which) -> {
                    try {
                        double h = Double.parseDouble(etHeight.getText().toString().trim());
                        if (h > 50 && h < 250) {
                            saveHeight(h);
                        }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveWeight(double weight) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            UserProfileEntity profile = db.userProfileDao().getNow();
            if (profile == null) profile = new UserProfileEntity();
            profile.weightKg = weight;
            db.userProfileDao().upsert(profile);
            AppExecutors.mainThread().execute(() -> {
                Toast.makeText(requireContext(), "Đã cập nhật cân nặng! ⚖️", Toast.LENGTH_SHORT).show();
                loadUserData();
            });
        });
    }

    private void saveHeight(double height) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            UserProfileEntity profile = db.userProfileDao().getNow();
            if (profile == null) profile = new UserProfileEntity();
            profile.heightCm = height;
            db.userProfileDao().upsert(profile);
            AppExecutors.mainThread().execute(() -> {
                Toast.makeText(requireContext(), "Đã cập nhật chiều cao!", Toast.LENGTH_SHORT).show();
                loadUserData();
            });
        });
    }
}
