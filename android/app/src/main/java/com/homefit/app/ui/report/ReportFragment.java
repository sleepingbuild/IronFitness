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

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.BmiCalculator;

public class ReportFragment extends Fragment {

    private TextView tvWorkoutCount;
    private TextView tvKcalCount;
    private TextView tvMinutesCount;
    private TextView tvStreak;
    private TextView tvBestStreak;
    private TextView tvCurrentWeight;
    private TextView tvMaxWeight;
    private TextView tvMinWeight;
    private TextView tvBmiValue;
    private TextView tvHeightValue;

    private UserProfileEntity userProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvKcalCount = view.findViewById(R.id.tvKcalCount);
        tvMinutesCount = view.findViewById(R.id.tvMinutesCount);
        tvStreak = view.findViewById(R.id.tvStreak);
        tvBestStreak = view.findViewById(R.id.tvBestStreak);
        tvCurrentWeight = view.findViewById(R.id.tvCurrentWeight);
        tvMaxWeight = view.findViewById(R.id.tvMaxWeight);
        tvMinWeight = view.findViewById(R.id.tvMinWeight);
        tvBmiValue = view.findViewById(R.id.tvBmiValue);
        tvHeightValue = view.findViewById(R.id.tvHeightValue);

        MaterialButton btnLogWeight = view.findViewById(R.id.btnLogWeight);
        MaterialButton btnEditBmi = view.findViewById(R.id.btnEditBmi);

        btnLogWeight.setOnClickListener(v -> showWeightDialog());
        btnEditBmi.setOnClickListener(v -> showHeightDialog());
        tvHeightValue.setOnClickListener(v -> showHeightDialog());

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
            if (userProfile == null) {
                userProfile = new UserProfileEntity();
                userProfile.email = "qua@gmail.com";
                userProfile.displayName = "Member Test";
                userProfile.isPremium = true;
                db.userProfileDao().upsert(userProfile);
            }

            final int count = db.workoutLogDao().getCountNow();
            final int totalSec = db.workoutLogDao().getTotalSecondsNow();

            AppExecutors.mainThread().execute(() -> {
                updateUi(count, totalSec);
            });
        });
    }

    private void updateUi(int workoutCount, int totalSeconds) {
        if (userProfile == null || !isAdded()) return;

        int displayCount = Math.max(workoutCount, 2); // default demo 2
        int totalMinutes = Math.max(totalSeconds / 60, displayCount * 8);
        int totalKcal = totalMinutes * 6;

        tvWorkoutCount.setText(String.valueOf(displayCount));
        tvKcalCount.setText(String.valueOf(totalKcal));
        tvMinutesCount.setText(String.valueOf(totalMinutes));

        tvStreak.setText("1");
        tvBestStreak.setText("1 ngày");

        if (userProfile.weightKg != null && userProfile.weightKg > 0) {
            String wStr = String.format(java.util.Locale.US, "%.1f kg", userProfile.weightKg);
            tvCurrentWeight.setText(wStr);
            tvMaxWeight.setText("Nặng nhất  " + wStr);
            tvMinWeight.setText("Nhẹ nhất  " + wStr);
        } else {
            tvCurrentWeight.setText("--");
            tvMaxWeight.setText("Nặng nhất  --");
            tvMinWeight.setText("Nhẹ nhất  --");
        }

        if (userProfile.heightCm != null && userProfile.heightCm > 0) {
            tvHeightValue.setText(String.format(java.util.Locale.US, "%.0f cm ✏️", userProfile.heightCm));
        } else {
            tvHeightValue.setText("Chỉnh sửa ✏️");
        }

        if (userProfile.heightCm != null && userProfile.heightCm > 0 && userProfile.weightKg != null && userProfile.weightKg > 0) {
            double bmi = BmiCalculator.calculate(userProfile.heightCm, userProfile.weightKg);
            tvBmiValue.setText(String.format(java.util.Locale.US, "%.1f", bmi));
        } else {
            tvBmiValue.setText("--");
        }
    }

    private void showWeightDialog() {
        final EditText etWeight = new EditText(requireContext());
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (userProfile != null && userProfile.weightKg != null) {
            etWeight.setText(String.valueOf(userProfile.weightKg));
        } else {
            etWeight.setText("65.0");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Ghi nhận cân nặng (kg)")
                .setView(etWeight)
                .setPositiveButton("Lưu", (dialog, which) -> {
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
        if (userProfile != null && userProfile.heightCm != null) {
            etHeight.setText(String.valueOf(userProfile.heightCm));
        } else {
            etHeight.setText("170.0");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa chiều cao (cm)")
                .setView(etHeight)
                .setPositiveButton("Lưu", (dialog, which) -> {
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
        if (userProfile == null) return;
        userProfile.weightKg = weight;
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            db.userProfileDao().upsert(userProfile);
            AppExecutors.mainThread().execute(() -> {
                Toast.makeText(requireContext(), "Đã lưu cân nặng", Toast.LENGTH_SHORT).show();
                loadUserData();
            });
        });
    }

    private void saveHeight(double height) {
        if (userProfile == null) return;
        userProfile.heightCm = height;
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            db.userProfileDao().upsert(userProfile);
            AppExecutors.mainThread().execute(() -> {
                Toast.makeText(requireContext(), "Đã lưu chiều cao", Toast.LENGTH_SHORT).show();
                loadUserData();
            });
        });
    }
}
