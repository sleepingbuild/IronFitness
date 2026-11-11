package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.repository.ChallengeRepository;

public class ChallengeWeekFragment extends Fragment {

    private ChallengeRepository repository;

    private TextView tvLevelChip;
    private TextView tvSessionsRemaining;
    private TextView tvProgressPercent;
    private ProgressBar pbChallenge;
    private TextView tvMotivationQuote;
    private LinearLayout llWeeksContainer;

    private String currentLevel = "easy";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_challenge_week, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ChallengeRepository(requireContext());

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);
        tvLevelChip = view.findViewById(R.id.tvLevelChip);
        tvSessionsRemaining = view.findViewById(R.id.tvSessionsRemaining);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        pbChallenge = view.findViewById(R.id.pbChallenge);
        tvMotivationQuote = view.findViewById(R.id.tvMotivationQuote);
        llWeeksContainer = view.findViewById(R.id.llWeeksContainer);
        MaterialButton btnGo = view.findViewById(R.id.btnGo);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        btnMenu.setOnClickListener(v -> showDifficultyBottomSheet());

        btnGo.setOnClickListener(v -> {
            int currentDay = repository.getCurrentDayIndex(currentLevel);
            openDayScreen(currentDay);
        });

        loadChallengeData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChallengeData();
    }

    private void loadChallengeData() {
        currentLevel = repository.getSelectedLevel();
        String levelTitle = repository.getLevelTitleVi(currentLevel);
        tvLevelChip.setText(levelTitle + " ⚡");

        int completedCount = repository.getCompletedCount(currentLevel);
        int remaining = 24 - completedCount;
        int percent = (int) Math.round((completedCount / 24.0) * 100);

        tvSessionsRemaining.setText(remaining + " buổi tập còn lại");
        tvProgressPercent.setText(percent + "%");
        pbChallenge.setProgress(percent);

        if (completedCount == 0) {
            tvMotivationQuote.setText("Mỗi buổi một bước. 4 tuần để thay đổi bản thân. Bắt đầu thôi!");
        } else if (completedCount < 24) {
            tvMotivationQuote.setText("Bạn đã hoàn thành " + completedCount + "/24 buổi. Hãy tiếp tục cố gắng!");
        } else {
            tvMotivationQuote.setText("Chúc mừng! Bạn đã hoàn thành Thử thách bản thân độ khó " + levelTitle + "!");
        }

        renderWeeksTimeline();
    }

    private void renderWeeksTimeline() {
        llWeeksContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        int currentUnlockedDay = repository.getCurrentDayIndex(currentLevel);

        for (int week = 1; week <= 4; week++) {
            final int currentWeek = week;
            View weekView = inflater.inflate(R.layout.item_challenge_week, llWeeksContainer, false);
            TextView tvWeekTitle = weekView.findViewById(R.id.tvWeekTitle);
            TextView tvWeekProgress = weekView.findViewById(R.id.tvWeekProgress);
            GridLayout gridSlots = weekView.findViewById(R.id.gridSlots);

            tvWeekTitle.setText("Tuần " + week);

            int weekCompletedCount = 0;
            for (int pos = 1; pos <= 6; pos++) {
                int dayIndex = (week - 1) * 7 + pos;
                if (repository.isDayCompleted(currentLevel, dayIndex)) {
                    weekCompletedCount++;
                }
            }
            tvWeekProgress.setText(weekCompletedCount + "/6");

            // Build 8 slot items
            for (int pos = 1; pos <= 8; pos++) {
                View slotView = inflater.inflate(R.layout.item_challenge_slot, gridSlots, false);
                FrameLayout flSlotBg = slotView.findViewById(R.id.flSlotBackground);
                TextView tvSlotText = slotView.findViewById(R.id.tvSlotText);
                ImageView ivSlotIcon = slotView.findViewById(R.id.ivSlotIcon);

                if (pos <= 7) {
                    int dayIndex = (week - 1) * 7 + pos;

                    if (repository.isRestDay(dayIndex)) {
                        // Rest Day
                        tvSlotText.setText("🌙");
                        flSlotBg.setBackgroundResource(R.drawable.bg_chip_inactive);
                        flSlotBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEDE9FE));
                        slotView.setOnClickListener(v -> showRestDayDialog(dayIndex));
                    } else if (repository.isDayCompleted(currentLevel, dayIndex)) {
                        // Completed Day
                        tvSlotText.setVisibility(View.GONE);
                        ivSlotIcon.setVisibility(View.VISIBLE);
                        ivSlotIcon.setImageResource(R.drawable.ic_check_circle_24);
                        flSlotBg.setBackgroundResource(R.drawable.bg_chip_selected);
                        flSlotBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF0066FF));
                        slotView.setOnClickListener(v -> openDayScreen(dayIndex));
                    } else if (dayIndex == currentUnlockedDay) {
                        // Current Active Day
                        tvSlotText.setText(String.valueOf(pos));
                        tvSlotText.setTextColor(requireContext().getColor(R.color.hf_primary));
                        flSlotBg.setBackgroundResource(R.drawable.bg_chip_active);
                        slotView.setOnClickListener(v -> openDayScreen(dayIndex));
                    } else {
                        // Locked Day
                        tvSlotText.setText("🔒");
                        tvSlotText.setTextSize(12);
                        flSlotBg.setBackgroundResource(R.drawable.bg_chip_inactive);
                        slotView.setOnClickListener(v -> Toast.makeText(requireContext(), "Hãy hoàn thành Ngày " + currentUnlockedDay + " trước", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Slot 8: Trophy
                    boolean weekDone = weekCompletedCount == 6;
                    tvSlotText.setText("🏆");
                    flSlotBg.setBackgroundResource(R.drawable.bg_chip_inactive);
                    if (weekDone) {
                        flSlotBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEF3C7));
                    }
                    slotView.setOnClickListener(v -> {
                        String status = weekDone ? "Đã hoàn thành xuất sắc!" : "Hoàn thành 6 buổi tập để nhận Cúp tuần này.";
                        Toast.makeText(requireContext(), "Cúp Tuần " + currentWeek + ": " + status, Toast.LENGTH_SHORT).show();
                    });
                }

                gridSlots.addView(slotView);
            }

            llWeeksContainer.addView(weekView);
        }
    }

    private void showRestDayDialog(int dayIndex) {
        int nextDay = dayIndex + 1;
        new AlertDialog.Builder(requireContext())
                .setTitle("Ngày nghỉ 🌙")
                .setMessage("Cơ bắp phát triển khi nghỉ ngơi. Bạn có thể nghỉ ngơi hoặc tập buổi tiếp theo ngay.")
                .setPositiveButton("Tập buổi tiếp theo", (dialog, which) -> openDayScreen(nextDay))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showDifficultyBottomSheet() {
        ChallengeDifficultyBottomSheetFragment bottomSheet = new ChallengeDifficultyBottomSheetFragment();
        bottomSheet.setOnDifficultyChangedListener(newLevel -> loadChallengeData());
        bottomSheet.show(getChildFragmentManager(), "DifficultyBottomSheet");
    }

    private void openDayScreen(int dayIndex) {
        Bundle args = new Bundle();
        args.putString("level", currentLevel);
        args.putInt("dayIndex", dayIndex);
        NavHostFragment.findNavController(this).navigate(R.id.action_challengeWeek_to_challengeDay, args);
    }
}
