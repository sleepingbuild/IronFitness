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
        String groupKey = getArguments() != null ? getArguments().getString("groupKey", "") : "";
        String difficulty = getArguments() != null ? getArguments().getString("difficulty", "") : "";
        String groupName = getArguments() != null ? getArguments().getString("groupName", "") : "";
        String customWorkoutId = getArguments() != null ? getArguments().getString("customWorkoutId", null) : null;

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
