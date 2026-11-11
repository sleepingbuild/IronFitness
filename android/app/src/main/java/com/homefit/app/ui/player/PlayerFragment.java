package com.homefit.app.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.homefit.app.R;
import com.homefit.app.ads.AdsManager;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.data.prefs.WorkoutProgressStore;
import com.homefit.app.data.repository.ChallengeRepository;
import com.homefit.app.databinding.FragmentPlayerBinding;
import com.homefit.app.ui.common.BannerAdHelper;
import com.homefit.app.ui.common.MediaLoader;

public class PlayerFragment extends Fragment {

    private FragmentPlayerBinding binding;
    private PlayerViewModel viewModel;
    private ExoPlayer exoPlayer;
    private final BannerAdHelper restBannerAdHelper = new BannerAdHelper();

    private String groupKey;
    private String difficulty;
    private String groupName;
    private String customWorkoutId;
    private String workoutKey;
    private String workoutTitle;
    private int resumeIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        binding.videoPlayerView.setPlayer(exoPlayer);
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);

        Bundle args = getArguments();
        if (args != null) {
            customWorkoutId = args.getString("customWorkoutId", null);
            groupKey = args.getString("groupKey", "");
            difficulty = args.getString("difficulty", "");
            groupName = args.getString("groupName", "");
            resumeIndex = args.getInt("resumeIndex", 0);

            if (customWorkoutId != null && !customWorkoutId.isEmpty()) {
                workoutKey = customWorkoutId;
                workoutTitle = "Tùy chỉnh";
                viewModel.loadCustomWorkout(customWorkoutId, resumeIndex);
            } else {
                workoutKey = groupKey + "_" + difficulty;
                workoutTitle = groupName + " " + difficulty;
                if (!groupKey.isEmpty() && !difficulty.isEmpty()) {
                    viewModel.loadExercises(groupKey, difficulty, groupName, resumeIndex);
                }
            }
        }

        setupButtons();
        setupBackPressHandler();
        observeViewModel();
    }

    private void setupButtons() {
        binding.btnBackExit.setOnClickListener(v -> handleBackExit());
        binding.btnPlayPause.setOnClickListener(v -> viewModel.onPlayPauseClicked());
        binding.btnNext.setOnClickListener(v -> viewModel.onNextClicked());
        binding.btnPrevious.setOnClickListener(v -> viewModel.onPreviousClicked());
        binding.btnDoneReps.setOnClickListener(v -> viewModel.onRepsDoneClicked());

        // Rest screen controls (Image 1)
        binding.btnAddRest20s.setOnClickListener(v -> viewModel.addRestSeconds(20));
        binding.btnSkipRest.setOnClickListener(v -> viewModel.skipRest());
        binding.btnEditRestTime.setOnClickListener(v -> showEditRestTimeDialog());
    }

    private void setupBackPressHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackExit();
            }
        });
    }

    private void handleBackExit() {
        viewModel.pauseWorkout();

        new AlertDialog.Builder(requireContext())
                .setTitle("Thoát buổi tập?")
                .setMessage("Bạn có chắc chắn muốn thoát buổi tập này không?")
                .setPositiveButton("Có", (dialog, which) -> showSaveProgressOptionsDialog())
                .setNegativeButton("Không", (dialog, which) -> viewModel.onPlayPauseClicked())
                .show();
    }

    private void showSaveProgressOptionsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Thoát buổi tập")
                .setMessage("Bạn có muốn lưu tiến độ để lần sau tập tiếp không?")
                .setPositiveButton("Lưu tiến độ", (dialog, which) -> {
                    saveWorkoutProgress();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .setNegativeButton("Khởi động lại", (dialog, which) -> {
                    clearWorkoutProgress();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void saveWorkoutProgress() {
        PlayerUiState state = viewModel.getUiState().getValue();
        if (state != null && state.totalExercises > 0 && workoutKey != null) {
            WorkoutProgressStore store = new WorkoutProgressStore(requireContext());
            store.saveProgress(workoutKey, workoutTitle, state.currentExerciseIndex, state.totalExercises);
        }
    }

    private void clearWorkoutProgress() {
        if (workoutKey != null) {
            WorkoutProgressStore store = new WorkoutProgressStore(requireContext());
            store.clearProgress(workoutKey);
        }
    }

    private void showEditRestTimeDialog() {
        final android.widget.EditText etRest = new android.widget.EditText(requireContext());
        etRest.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etRest.setText("20");

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa thời gian nghỉ")
                .setMessage("Nhập số giây muốn cộng thêm:")
                .setView(etRest)
                .setPositiveButton("Cộng thêm", (dialog, which) -> {
                    try {
                        int seconds = Integer.parseInt(etRest.getText().toString().trim());
                        if (seconds > 0) {
                            viewModel.addRestSeconds(seconds);
                        }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.state == PlayerState.FINISHED) {
                clearWorkoutProgress();
                markChallengeCompletedIfAny();
                // Show Interstitial Ad before navigating to Summary Screen
                AdsManager.showInterstitial(requireActivity(), () -> navigateToSummary(state), true);
                return;
            }

            updateUi(state);
        });
    }

    private void markChallengeCompletedIfAny() {
        if (groupKey != null && groupKey.startsWith("challenge_")) {
            try {
                String level = groupKey.replace("challenge_", "");
                int dayIndex = Integer.parseInt(difficulty);
                ChallengeRepository challengeRepo = new ChallengeRepository(requireContext());
                challengeRepo.markDayCompleted(level, dayIndex);
            } catch (Exception ignored) {}
        }
    }

    private void navigateToSummary(PlayerUiState state) {
        if (!isAdded()) return;
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putInt("totalExercises", state.totalExercises);
        args.putInt("totalSeconds", state.totalWorkoutSeconds);
        args.putString("groupKey", groupKey);
        args.putString("difficulty", difficulty);
        args.putString("groupName", groupName);
        if (customWorkoutId != null) {
            args.putString("customWorkoutId", customWorkoutId);
        }
        navController.navigate(R.id.workoutSummaryFragment, args);
    }

    private void updateUi(PlayerUiState state) {
        LocalWorkoutDto current = state.currentExercise;

        if (state.state == PlayerState.RESTING) {
            binding.clExercisingLayout.setVisibility(View.GONE);
            binding.llRestingLayout.setVisibility(View.VISIBLE);

            binding.tvNextProgress.setText("TIẾP THEO " + (state.currentExerciseIndex + 1) + "/" + state.totalExercises);
            binding.tvNextExerciseName.setText(current != null ? current.name : "");
            binding.tvNextRepsSubtitle.setText(current != null ? current.reps_sets : "");
            binding.tvRestCounter.setText(formatTime(state.remainingSeconds));

            if (current != null && current.id != null) {
                MediaLoader.loadThumbnail(binding.ivNextThumb, "", "IMAGE");
            }

            // Attach Banner Ad to bottom resting container
            restBannerAdHelper.attach(binding.adContainerResting);
            restBannerAdHelper.onResume();

        } else {
            binding.clExercisingLayout.setVisibility(View.VISIBLE);
            binding.llRestingLayout.setVisibility(View.GONE);

            binding.tvProgress.setText("Bài " + (state.currentExerciseIndex + 1) + " / " + state.totalExercises);

            switch (state.state) {
                case PREPARING:
                    binding.tvStateHeader.setText("Chuẩn bị");
                    binding.tvExerciseName.setText(current != null ? current.name : "");
                    binding.tvCounter.setText(formatTime(state.remainingSeconds));
                    showControls(false);
                    break;
                case EXERCISING:
                    binding.tvStateHeader.setText("Tập luyện");
                    binding.tvExerciseName.setText(current != null ? current.name : "");
                    if (state.isRepsBased) {
                        binding.tvCounter.setText(current != null ? current.reps_sets : "");
                    } else {
                        binding.tvCounter.setText(formatTime(state.remainingSeconds));
                    }
                    showControls(state.isRepsBased);
                    break;
                case PAUSED:
                    binding.tvStateHeader.setText("Tạm dừng");
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow_24);
                    break;
                default:
                    break;
            }

            if (state.state != PlayerState.PAUSED) {
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24);
            }

            binding.videoPlayerView.setVisibility(View.GONE);
            binding.ivMedia.setVisibility(View.VISIBLE);
            binding.ivMedia.setImageResource(R.drawable.bg_media_placeholder);
        }
    }

    private void showControls(boolean isRepsBased) {
        if (isRepsBased) {
            binding.llControls.setVisibility(View.GONE);
            binding.btnDoneReps.setVisibility(View.VISIBLE);
        } else {
            binding.llControls.setVisibility(View.VISIBLE);
            binding.btnDoneReps.setVisibility(View.GONE);
        }
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format(java.util.Locale.US, "%02d:%02d", min, sec);
    }

    @Override
    public void onResume() {
        super.onResume();
        restBannerAdHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        restBannerAdHelper.onPause();
    }

    @Override
    public void onDestroyView() {
        restBannerAdHelper.destroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
