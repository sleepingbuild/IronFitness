package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.repository.ChallengeRepository;
import com.homefit.app.databinding.FragmentHomeWorkoutBinding;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.StreakCalculator;

import java.util.List;

public class HomeWorkoutFragment extends Fragment {

    private FragmentHomeWorkoutBinding binding;
    private HomeWorkoutViewModel viewModel;
    private HomeWorkoutAdapter popularAdapter;
    private HomeWorkoutAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeWorkoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeWorkoutViewModel.class);

        popularAdapter = new HomeWorkoutAdapter(this::onGroupClicked);
        if (binding.rvPopularGoals != null) {
            binding.rvPopularGoals.setAdapter(popularAdapter);
        }

        categoryAdapter = new HomeWorkoutAdapter(this::onGroupClicked);
        if (binding.rvWorkouts != null) {
            binding.rvWorkouts.setAdapter(categoryAdapter);
        }

        setupChipListeners();
        setupMockActions();

        View btnProBadge = view.findViewById(R.id.btnProBadge);
        if (btnProBadge != null) {
            btnProBadge.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_global_proPaywall));
        }

        viewModel.getPopularGoalsData().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null && popularAdapter != null) {
                popularAdapter.submitList(groups);
            }
        });

        updateProStateUI();
        updateStreakUI();
        updateChallengeHeroUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProStateUI();
        updateStreakUI();
        updateChallengeHeroUI();
    }

    private void updateChallengeHeroUI() {
        if (!isAdded() || binding == null || binding.cardFirstDay == null) return;
        ChallengeRepository challengeRepo = new ChallengeRepository(requireContext());
        String level = challengeRepo.getSelectedLevel();

        int completed = challengeRepo.getCompletedCount(level);
        int currentDay = challengeRepo.getCurrentDayIndex(level);
        String dayTypeName = challengeRepo.getDayTypeName(currentDay);

        LocalExerciseGroupDto session = challengeRepo.getSessionForDay(level, currentDay);
        int durationMin = session != null ? session.duration_min : 11;
        int count = session != null && session.workouts != null ? session.workouts.size() : 14;

        binding.tvFirstDayLabel.setText("THỬ THÁCH BẢN THÂN");
        binding.tvFirstDayTitle.setText("Ngày " + currentDay + " - " + dayTypeName);
        binding.tvFirstDaySubtitle.setText(durationMin + " phút • " + count + " bài");

        int percent = (int) Math.round((completed / 24.0) * 100);
        binding.pbFirstDay.setProgress(percent);
        binding.tvFirstDayProgress.setText(completed + "/24 buổi");

        binding.cardFirstDay.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_challengeWeek));

        if (binding.btnStartChallenge != null) {
            binding.btnStartChallenge.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("level", level);
                args.putInt("dayIndex", currentDay);
                NavHostFragment.findNavController(this).navigate(R.id.action_home_to_challengeDay, args);
            });
        }
    }

    private void updateStreakUI() {
        if (!isAdded()) return;
        AppExecutors.diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Long> completedTimes = db.workoutLogDao().getAllCompletedTimesNow();
            StreakCalculator.Result streakResult = StreakCalculator.calculateStreak(completedTimes);

            AppExecutors.mainThread().execute(() -> {
                if (binding != null) {
                    binding.tvStreakCount.setText(String.valueOf(streakResult.streakCount));
                    if (streakResult.isFlameActive) {
                        int activeColor = requireContext().getColor(R.color.hf_streak);
                        binding.ivStreakFire.setColorFilter(activeColor);
                        binding.tvStreakCount.setTextColor(activeColor);
                    } else {
                        int grayColor = requireContext().getColor(android.R.color.darker_gray);
                        binding.ivStreakFire.setColorFilter(grayColor);
                        binding.tvStreakCount.setTextColor(grayColor);
                    }
                }
            });
        });
    }

    private void updateProStateUI() {
        if (!isAdded()) return;
        AppExecutors.diskIO().execute(() -> {
            UserProfileEntity profile = AppDatabase.getInstance(requireContext()).userProfileDao().getNow();
            boolean isPro = profile != null && profile.isPremium;
            AppExecutors.mainThread().execute(() -> {
                if (binding != null) {
                    if (isPro) {
                        binding.ivProIcon.setImageResource(R.drawable.ic_crown);
                    } else {
                        binding.ivProIcon.setImageResource(R.drawable.ic_arrow_up);
                    }
                }
            });
        });
    }

    private void setupChipListeners() {
        if (binding.chipBung != null) binding.chipBung.setOnClickListener(v -> selectChip("bung"));
        if (binding.chipTay != null) binding.chipTay.setOnClickListener(v -> selectChip("tay"));
        if (binding.chipNguc != null) binding.chipNguc.setOnClickListener(v -> selectChip("nguc"));
        if (binding.chipChan != null) binding.chipChan.setOnClickListener(v -> selectChip("chan"));
        if (binding.chipToanThan != null) binding.chipToanThan.setOnClickListener(v -> selectChip("toan_than"));

        selectPopularGoal("muscle");
        selectChip("bung");
    }

    private void openFilter(String filterType, String title) {
        Bundle args = new Bundle();
        args.putString("filterType", filterType);
        args.putString("title", title);
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_filteredGroups, args);
    }

    private void setupMockActions() {
        if (binding.cardCustomWorkout != null) {
            binding.cardCustomWorkout.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_home_to_customWorkout));
        }
        if (binding.btnMoreForYou != null) {
            binding.btnMoreForYou.setOnClickListener(v -> openFilter("foryou", "Chỉ dành cho bạn"));
        }
        if (binding.btnMoreStretch != null) {
            binding.btnMoreStretch.setOnClickListener(v -> openFilter("short_workout", "Buổi ngắn"));
        }
        if (binding.cardTabata != null) {
            binding.cardTabata.setOnClickListener(v -> openFilter("tabata", "6 phút với Tabata"));
        }
        if (binding.cardStretchBack != null) {
            binding.cardStretchBack.setOnClickListener(v -> openFilter("stretch_back", "Kéo giãn Lưng"));
        }
        if (binding.chipGoalBuildMuscle != null) {
            binding.chipGoalBuildMuscle.setOnClickListener(v -> selectPopularGoal("muscle"));
        }
        if (binding.chipGoalFatLoss != null) {
            binding.chipGoalFatLoss.setOnClickListener(v -> selectPopularGoal("fatloss"));
        }
        if (binding.chipGoalKeepFit != null) {
            binding.chipGoalKeepFit.setOnClickListener(v -> selectPopularGoal("keepfit"));
        }
        if (binding.llForYou != null) {
            binding.llForYouCalorie.setOnClickListener(v -> openFilter("calorie_burn", "Đốt cháy calo toàn thân"));
            binding.llForYouLeg.setOnClickListener(v -> openFilter("leg_strength", "Thử thách sức mạnh chân"));
        }
    }

    private void selectPopularGoal(String goalType) {
        resetChip(binding.chipGoalBuildMuscle);
        resetChip(binding.chipGoalFatLoss);
        resetChip(binding.chipGoalKeepFit);

        switch (goalType) {
            case "muscle": setActiveChip(binding.chipGoalBuildMuscle); break;
            case "fatloss": setActiveChip(binding.chipGoalFatLoss); break;
            case "keepfit": setActiveChip(binding.chipGoalKeepFit); break;
        }

        viewModel.loadPopularGoals(goalType);
    }

    private void selectChip(String groupKey) {
        resetChip(binding.chipBung);
        resetChip(binding.chipTay);
        resetChip(binding.chipNguc);
        resetChip(binding.chipChan);
        resetChip(binding.chipToanThan);

        switch (groupKey) {
            case "bung": setActiveChip(binding.chipBung); break;
            case "tay": setActiveChip(binding.chipTay); break;
            case "nguc": setActiveChip(binding.chipNguc); break;
            case "chan": setActiveChip(binding.chipChan); break;
            case "toan_than": setActiveChip(binding.chipToanThan); break;
        }

        if (categoryAdapter != null) {
            categoryAdapter.submitList(viewModel.getGroupsByTarget(groupKey));
        }
    }

    private void resetChip(android.widget.TextView tv) {
        if (tv == null) return;
        tv.setBackgroundResource(R.drawable.bg_chip_inactive);
        tv.setTextColor(requireContext().getColor(R.color.hf_on_surface_variant));
        tv.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void setActiveChip(android.widget.TextView tv) {
        if (tv == null) return;
        tv.setBackgroundResource(R.drawable.bg_chip_active);
        tv.setTextColor(requireContext().getColor(R.color.hf_primary));
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void onGroupClicked(LocalExerciseGroupDto group) {
        if (group != null && ("muscle".equalsIgnoreCase(group.group) || "fatloss".equalsIgnoreCase(group.group) || "keepfit".equalsIgnoreCase(group.group))) {
            AppExecutors.diskIO().execute(() -> {
                UserProfileEntity profile = AppDatabase.getInstance(requireContext()).userProfileDao().getNow();
                boolean isPro = profile != null && profile.isPremium;

                AppExecutors.mainThread().execute(() -> {
                    if (!isPro) {
                        Bundle args = new Bundle();
                        args.putString("title", group.group_name + " " + group.difficulty);
                        args.putString("duration", group.duration_min + " phút");
                        int count = group.workouts != null ? group.workouts.size() : 13;
                        args.putString("count", count + " Bài tập");
                        NavHostFragment.findNavController(this).navigate(R.id.action_global_lockedWorkout, args);
                    } else {
                        openGroup(group);
                    }
                });
            });
        } else {
            openGroup(group);
        }
    }

    private void openGroup(LocalExerciseGroupDto group) {
        Bundle args = new Bundle();
        args.putString("groupKey", group.group);
        args.putString("difficulty", group.difficulty);
        args.putString("groupName", group.group_name);
        NavHostFragment.findNavController(this).navigate(R.id.action_category_to_exerciseList, args);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
