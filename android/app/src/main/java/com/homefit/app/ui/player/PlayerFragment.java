package com.homefit.app.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.homefit.app.R;
import com.homefit.app.databinding.FragmentPlayerBinding;
import com.homefit.app.data.local.entity.ExerciseEntity;

public class PlayerFragment extends Fragment {

    private FragmentPlayerBinding binding;
    private PlayerViewModel viewModel;

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

        long categoryId = getArguments() != null ? getArguments().getLong("categoryId", -1) : -1;
        if (categoryId != -1) {
            viewModel.loadExercises(categoryId);
        }

        setupButtons();
        observeViewModel();
    }

    private void setupButtons() {
        binding.btnPlayPause.setOnClickListener(v -> viewModel.onPlayPauseClicked());
        binding.btnNext.setOnClickListener(v -> viewModel.onNextClicked());
        binding.btnPrevious.setOnClickListener(v -> viewModel.onPreviousClicked());
        binding.btnDoneReps.setOnClickListener(v -> viewModel.onRepsDoneClicked());
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.state == PlayerState.FINISHED) {
                // Navigate to summary
                NavController navController = Navigation.findNavController(requireView());
                Bundle args = new Bundle();
                args.putInt("totalExercises", state.totalExercises);
                args.putInt("totalSeconds", state.totalWorkoutSeconds);
                args.putLong("categoryId", getArguments() != null ? getArguments().getLong("categoryId", -1) : -1);
                // Táº¡o action tá»›i Workout Summary (cáº§n update nav_graph.xml)
                navController.navigate(R.id.workoutSummaryFragment, args);
                return;
            }

            updateUi(state);
        });
    }

    private void updateUi(PlayerUiState state) {
        ExerciseEntity current = state.currentExercise;
        
        binding.tvProgress.setText("BÃ i " + (state.currentExerciseIndex + 1) + " / " + state.totalExercises);

        switch (state.state) {
            case PREPARING:
                binding.tvStateHeader.setText("Chuáº©n bá»‹");
                binding.tvExerciseName.setText(current != null ? current.name : "");
                binding.tvCounter.setText(formatTime(state.remainingSeconds));
                showControls(false);
                break;
            case RESTING:
                binding.tvStateHeader.setText("Nghá»‰ ngÆ¡i");
                binding.tvExerciseName.setText("Tiáº¿p theo: " + (current != null ? current.name : ""));
                binding.tvCounter.setText(formatTime(state.remainingSeconds));
                showControls(false);
                break;
            case EXERCISING:
                binding.tvStateHeader.setText("Táº­p luyá»‡n");
                binding.tvExerciseName.setText(current != null ? current.name : "");
                if (state.isRepsBased) {
                    binding.tvCounter.setText(current != null ? "x" + current.reps : "");
                } else {
                    binding.tvCounter.setText(formatTime(state.remainingSeconds));
                }
                showControls(state.isRepsBased);
                break;
            case PAUSED:
                binding.tvStateHeader.setText("Táº¡m dá»«ng");
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24); // Thay Ä‘á»•i icon thÃ nh Play
                break;
            default:
                break;
        }

        if (state.state != PlayerState.PAUSED) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24);
        }

        if (current != null && current.mediaUrl != null && (state.state == PlayerState.EXERCISING || state.state == PlayerState.PREPARING)) {
             Glide.with(this).load(current.mediaUrl).into(binding.ivMedia);
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
        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


