package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.model.CustomWorkout;
import com.homefit.app.data.repository.CustomWorkoutRepository;

import java.util.List;

public class CustomWorkoutFragment extends Fragment {

    private CustomWorkoutRepository repository;

    private LinearLayout llEmptyState;
    private LinearLayout llListState;
    private CustomWorkoutAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new CustomWorkoutRepository(requireContext());

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        llListState = view.findViewById(R.id.llListState);
        MaterialButton btnCreate = view.findViewById(R.id.btnCreate);
        MaterialButton btnCreateNewBottom = view.findViewById(R.id.btnCreateNewBottom);
        RecyclerView rvCustomWorkouts = view.findViewById(R.id.rvCustomWorkouts);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        adapter = new CustomWorkoutAdapter(new CustomWorkoutAdapter.OnCustomWorkoutClickListener() {
            @Override
            public void onStartWorkout(CustomWorkout workout) {
                startWorkoutPlayer(workout);
            }

            @Override
            public void onEditWorkout(CustomWorkout workout) {
                openEditWorkout(workout.id);
            }

            @Override
            public void onDeleteWorkout(CustomWorkout workout) {
                confirmDelete(workout);
            }
        });

        rvCustomWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCustomWorkouts.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> openEditWorkout(null));
        btnCreateNewBottom.setOnClickListener(v -> openEditWorkout(null));

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<CustomWorkout> workouts = repository.getAllWorkouts();
        if (workouts == null || workouts.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            llListState.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            llListState.setVisibility(View.VISIBLE);
            adapter.setItems(workouts);
        }
    }

    private void openEditWorkout(@Nullable String workoutId) {
        Bundle args = new Bundle();
        if (workoutId != null) {
            args.putString("workoutId", workoutId);
        }
        NavHostFragment.findNavController(this).navigate(R.id.action_customWorkout_to_editCustom, args);
    }

    private void startWorkoutPlayer(CustomWorkout workout) {
        if (workout == null) return;
        Bundle args = new Bundle();
        args.putString("customWorkoutId", workout.id);
        args.putString("groupKey", "custom");
        args.putString("difficulty", "Custom");
        NavHostFragment.findNavController(this).navigate(R.id.action_customWorkout_to_player, args);
    }

    private void confirmDelete(CustomWorkout workout) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa buổi tập")
                .setMessage("Bạn có chắc chắn muốn xóa \"" + workout.title + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.deleteWorkout(workout.id);
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
