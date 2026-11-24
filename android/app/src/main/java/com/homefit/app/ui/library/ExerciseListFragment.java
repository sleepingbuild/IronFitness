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
            displayHeaderBanner();
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
        if (categoryId == -1) {
            loadData();
        }
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
        if (!isAdded() || getContext() == null) return;
        final android.content.Context appContext = requireContext().getApplicationContext();

        com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
            LocalExerciseGroupDto roomGroup = com.homefit.app.data.repository.WorkoutLoaderHelper.loadWorkoutFromRoom(
                    appContext, groupKey, difficulty, groupName
            );

            com.homefit.app.util.AppExecutors.mainThread().execute(() -> {
                if (!isAdded() || binding == null || getContext() == null) return;

                if (roomGroup != null && roomGroup.workouts != null && !roomGroup.workouts.isEmpty()) {
                    currentGroup = roomGroup;
                } else {
                    AssetExerciseRepository repo = new AssetExerciseRepository(appContext);
                    List<LocalExerciseGroupDto> all = repo.getAllGroups();
                    if (all != null) {
                        for (LocalExerciseGroupDto g : all) {
                            if (g != null && g.group != null && g.group.equalsIgnoreCase(groupKey) && g.difficulty != null && g.difficulty.equalsIgnoreCase(difficulty)) {
                                currentGroup = g;
                                break;
                            }
                        }
                    }
                    if (currentGroup == null) {
                        currentGroup = getDynamicGroup(groupKey, difficulty, groupName);
                    }
                }

                if (currentGroup != null) {
                    android.content.Context ctx = requireContext();
                    String fullTitle = (currentGroup.group_name != null ? currentGroup.group_name : "") + " " + (currentGroup.difficulty != null ? currentGroup.difficulty : "");
                    binding.tvTitle.setText(com.homefit.app.util.LocalizationHelper.localizeWorkoutTitle(ctx, fullTitle.trim()));
                    int count = currentGroup.workouts != null ? currentGroup.workouts.size() : 0;

                    String subtitleStr = com.homefit.app.util.LocalizationHelper.localizeSubtitle(ctx, count, currentGroup.difficulty);
                    if (subtitleStr != null && subtitleStr.contains("•")) {
                        binding.tvDuration.setText(subtitleStr.split("•")[0].trim());
                    } else {
                        binding.tvDuration.setText(count + " phút");
                    }

                    String durCountStr = com.homefit.app.util.LocalizationHelper.getLocalizedDurationAndCount(ctx, count, count);
                    if (durCountStr != null && durCountStr.contains("•")) {
                        String[] parts = durCountStr.split("•");
                        if (parts.length > 1) {
                            binding.tvCount.setText(parts[1].trim());
                        } else {
                            binding.tvCount.setText(count + " Bài tập");
                        }
                    } else {
                        binding.tvCount.setText(count + " Bài tập");
                    }

                    adapter.submitList(currentGroup.workouts);
                    displayHeaderBanner();
                    binding.progress.setVisibility(View.GONE);
                    binding.stateView.getRoot().setVisibility(View.GONE);
                    binding.rvExercises.setVisibility(View.VISIBLE);
                } else {
                    binding.progress.setVisibility(View.GONE);
                    binding.stateView.getRoot().setVisibility(View.VISIBLE);
                    binding.stateView.tvStateMessage.setText("Không tìm thấy bài tập");
                }

                checkProgressAndSetupButtons();
            });
        });
    }

    private void displayHeaderBanner() {
        if (binding == null) return;
        String coverUrl = null;
        if (currentGroup != null && currentGroup.coverImageUrl != null
                && !currentGroup.coverImageUrl.isBlank()
                && !currentGroup.coverImageUrl.contains("unsplash.com")) {
            coverUrl = currentGroup.coverImageUrl;
        }
        if (coverUrl == null || coverUrl.isBlank()) {
            coverUrl = getMuscleCoverUrlForGroup(groupKey, groupName);
        }
        String fullUrl = com.homefit.app.ui.common.MediaLoader.resolveUrl(coverUrl);
        com.bumptech.glide.Glide.with(binding.ivHeaderBanner)
                .load(fullUrl)
                .fitCenter()
                .placeholder(R.drawable.bg_media_placeholder)
                .into(binding.ivHeaderBanner);
    }

    private String getMuscleCoverUrlForGroup(String groupKey, String groupName) {
        String base = "https://ironfitness-et3g.onrender.com/images/muscles/";
        String combined = (groupKey != null ? groupKey : "") + " " + (groupName != null ? groupName : "");
        String g = combined.toLowerCase(java.util.Locale.US);
        if (g.contains("bung") || g.contains("abs")) return base + "abs.png";
        if (g.contains("nguc") || g.contains("chest")) return base + "chest.png";
        if (g.contains("tay") || g.contains("arms")) return base + "arms.png";
        if (g.contains("chan") || g.contains("legs") || g.contains("mông")) return base + "legs.png";
        if (g.contains("back") || g.contains("lung") || g.contains("lưng")) return base + "back.png";
        return base + "fullbody.png";
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
        if (currentGroup.workouts != null && !currentGroup.workouts.isEmpty()) {
            com.homefit.app.ui.player.ActiveWorkoutHolder.getInstance().setActiveWorkouts(currentGroup.workouts);
        }
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
