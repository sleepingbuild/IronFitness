package com.homefit.app.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.homefit.app.R;
import com.homefit.app.data.local.AssetExerciseRepository;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.prefs.WorkoutProgressStore;
import com.homefit.app.databinding.FragmentExerciseListBinding;

import java.util.List;

public class ExerciseListFragment extends Fragment {

    private FragmentExerciseListBinding binding;
    private ExerciseListAdapter adapter;
    private String groupKey;
    private String difficulty;
    private String groupName;
    private LocalExerciseGroupDto currentGroup;

    private long categoryId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(LibraryArgs.CATEGORY_ID)) {
                categoryId = args.getLong(LibraryArgs.CATEGORY_ID, -1);
            }
            if (args.containsKey(LibraryArgs.CATEGORY_NAME)) {
                groupName = args.getString(LibraryArgs.CATEGORY_NAME, "");
            }
            if (args.containsKey("groupKey")) {
                groupKey = args.getString("groupKey");
            }
            if (args.containsKey("difficulty")) {
                difficulty = args.getString("difficulty");
            }
            if (groupName == null || groupName.isEmpty()) {
                groupName = args.getString("groupName", "");
            }
        }

        // Setup adapter
        adapter = new ExerciseListAdapter();
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExercises.setAdapter(adapter);

        // Setup buttons
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        if (categoryId != -1) {
            loadCategoryData();
        } else {
            loadData();
        }
    }

    private void loadCategoryData() {
        ExerciseListViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(ExerciseListViewModel.class);
        viewModel.init(categoryId);

        binding.stateView.btnStateAction.setOnClickListener(v -> viewModel.refresh());

        androidx.lifecycle.LiveData<java.util.List<com.homefit.app.data.local.entity.ExerciseEntity>> exercisesLiveData = viewModel.getExercises();
        if (exercisesLiveData != null) {
            exercisesLiveData.observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.progress.setVisibility(View.GONE);
                binding.stateView.getRoot().setVisibility(View.VISIBLE);
                binding.rvExercises.setVisibility(View.GONE);
                binding.stateView.tvStateMessage.setText("Chưa có bài tập nào trong nhóm này");
                return;
            }

            java.util.List<LocalExerciseGroupDto.LocalWorkoutDto> workouts = new java.util.ArrayList<>();
            for (com.homefit.app.data.local.entity.ExerciseEntity e : list) {
                LocalExerciseGroupDto.LocalWorkoutDto dto = new LocalExerciseGroupDto.LocalWorkoutDto();
                dto.id = String.valueOf(e.id);
                dto.name = e.name;
                dto.reps_sets = ExerciseText.work(requireContext(), e);
                dto.target = e.description != null ? e.description : "";
                workouts.add(dto);
            }

            currentGroup = new LocalExerciseGroupDto();
            currentGroup.group = "category_" + categoryId;
            currentGroup.group_name = (groupName != null && !groupName.isEmpty()) ? groupName : "Nhóm cơ";
            currentGroup.difficulty = "";
            currentGroup.workouts = workouts;

            binding.tvTitle.setText(currentGroup.group_name);
            int count = workouts.size();
            binding.tvDuration.setText(count + " phút");
            binding.tvCount.setText(count + " Bài tập");

            adapter.submitList(workouts);
            binding.progress.setVisibility(View.GONE);
            binding.stateView.getRoot().setVisibility(View.GONE);
            binding.rvExercises.setVisibility(View.VISIBLE);

            checkProgressAndSetupButtons();
        });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkProgressAndSetupButtons();
    }

    private void checkProgressAndSetupButtons() {
        WorkoutProgressStore progressStore = new WorkoutProgressStore(requireContext());
        String workoutKey = (groupKey != null ? groupKey : "") + "_" + (difficulty != null ? difficulty : "");
        WorkoutProgressStore.SavedProgress progress = progressStore.getProgress(workoutKey);

        if (progress != null && progress.completedIndex > 0) {
            binding.llSingleStartContainer.setVisibility(View.GONE);
            binding.llResumeContainer.setVisibility(View.VISIBLE);
            binding.tvProgressPercent.setText(progress.percentCompleted + "% đã hoàn thành");

            binding.btnRestart.setOnClickListener(v -> {
                progressStore.clearProgress(workoutKey);
                startWorkout(0);
            });

            binding.btnContinue.setOnClickListener(v -> startWorkout(progress.completedIndex));
        } else {
            binding.llSingleStartContainer.setVisibility(View.VISIBLE);
            binding.llResumeContainer.setVisibility(View.GONE);
            binding.btnStart.setOnClickListener(v -> startWorkout(0));
        }
    }

    private void loadData() {
        AssetExerciseRepository repo = new AssetExerciseRepository(requireContext());
        List<LocalExerciseGroupDto> all = repo.getAllGroups();
        for (LocalExerciseGroupDto g : all) {
            if (g.group.equalsIgnoreCase(groupKey) && g.difficulty.equalsIgnoreCase(difficulty)) {
                currentGroup = g;
                break;
            }
        }

        // Handle generated goals dynamically
        if (currentGroup == null) {
            currentGroup = getDynamicGroup(groupKey, difficulty, groupName);
        }

        if (currentGroup != null) {
            binding.tvTitle.setText(currentGroup.group_name + " " + currentGroup.difficulty);
            int count = currentGroup.workouts != null ? currentGroup.workouts.size() : 0;
            binding.tvDuration.setText(count + " phút");
            binding.tvCount.setText(count + " Bài tập");

            adapter.submitList(currentGroup.workouts);
            // Hide progress and state view
            binding.progress.setVisibility(View.GONE);
            binding.stateView.getRoot().setVisibility(View.GONE);
            binding.rvExercises.setVisibility(View.VISIBLE);
        } else {
            // Error handling
            binding.progress.setVisibility(View.GONE);
            binding.stateView.getRoot().setVisibility(View.VISIBLE);
            binding.stateView.tvStateMessage.setText("Không tìm thấy bài tập");
        }

        checkProgressAndSetupButtons();
    }

    private LocalExerciseGroupDto getDynamicGroup(String groupKey, String diff, String name) {
        if ("muscle".equals(groupKey)) {
            if ("Chân & Mông".equals(name)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createMuscleMedium2();
            if ("Thân trên + Lõi".equals(name)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createMuscleMedium3();
            return com.homefit.app.ui.workout.PopularWorkoutGenerator.createMuscleMedium1();
        } else if ("fatloss".equals(groupKey)) {
            if ("Easy".equalsIgnoreCase(diff)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createFatLossEasy();
            if ("Medium".equalsIgnoreCase(diff)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createFatLossMedium();
            if ("Hard".equalsIgnoreCase(diff)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createFatLossHard();
        } else if ("keepfit".equals(groupKey)) {
            if ("Easy".equalsIgnoreCase(diff)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createKeepFitEasy();
            if ("Medium".equalsIgnoreCase(diff)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createKeepFitMedium();
            if ("Hard".equalsIgnoreCase(diff)) return com.homefit.app.ui.workout.PopularWorkoutGenerator.createKeepFitHard();
        }
        return null;
    }

    private void startWorkout(int resumeIndex) {
        if (currentGroup == null) return;
        NavController navController = NavHostFragment.findNavController(this);
        Bundle args = new Bundle();
        args.putString("groupKey", groupKey);
        args.putString("difficulty", difficulty);
        args.putString("groupName", groupName);
        args.putInt("resumeIndex", resumeIndex);
        navController.navigate(R.id.action_exerciseList_to_player, args);
    }

    @Override
    public void onDestroyView() {
        binding.rvExercises.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
