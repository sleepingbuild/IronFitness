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
        
        int totalExercises = getArguments() != null ? getArguments().getInt("totalExercises", 0) : 0;
        int totalSeconds = getArguments() != null ? getArguments().getInt("totalSeconds", 0) : 0;
        
        binding.tvTotalExercises.setText(String.valueOf(totalExercises));
        binding.tvTotalTime.setText(formatTime(totalSeconds));
        
        binding.btnFinish.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            // Quay về màn hình danh sách Category
            navController.popBackStack(R.id.workoutFragment, false);
        });
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