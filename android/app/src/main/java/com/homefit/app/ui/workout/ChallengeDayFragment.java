package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.repository.ChallengeRepository;
import com.homefit.app.ui.library.ExerciseListAdapter;

public class ChallengeDayFragment extends Fragment {

    private ChallengeRepository repository;
    private String level = "easy";
    private int dayIndex = 1;
    private LocalExerciseGroupDto currentSession;

    private TextView tvDayTitle;
    private TextView tvDaySubtitle;
    private TextView tvDuration;
    private TextView tvCount;
    private MaterialButton btnStart;
    private RecyclerView rvExercises;
    private ExerciseListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_challenge_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ChallengeRepository(requireContext());

        if (getArguments() != null) {
            level = getArguments().getString("level", "easy");
            dayIndex = getArguments().getInt("dayIndex", 1);
        }

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);
        tvDayTitle = view.findViewById(R.id.tvDayTitle);
        tvDaySubtitle = view.findViewById(R.id.tvDaySubtitle);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvCount = view.findViewById(R.id.tvCount);
        btnStart = view.findViewById(R.id.btnStart);
        rvExercises = view.findViewById(R.id.rvExercises);

        adapter = new ExerciseListAdapter();
        rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExercises.setAdapter(adapter);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        btnMenu.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Tùy chọn")
                    .setItems(new String[]{"Khôi phục danh sách mặc định"}, (dialog, which) -> {
                        loadSessionData();
                        Toast.makeText(requireContext(), "Đã khôi phục danh sách bài tập", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        btnStart.setOnClickListener(v -> startWorkoutPlayer());

        loadSessionData();
    }

    private void loadSessionData() {
        currentSession = repository.getSessionForDay(level, dayIndex);
        if (currentSession != null) {
            tvDayTitle.setText("NGÀY " + dayIndex);
            tvDaySubtitle.setText(repository.getDayTypeName(dayIndex) + " - " + repository.getLevelTitleVi(level));
            tvDuration.setText(currentSession.duration_min + " phút");

            int count = currentSession.workouts != null ? currentSession.workouts.size() : 0;
            tvCount.setText(count + " Bài tập");

            adapter.submitList(currentSession.workouts);
        }
    }

    private void startWorkoutPlayer() {
        if (currentSession == null) return;
        NavController navController = NavHostFragment.findNavController(this);
        Bundle args = new Bundle();
        args.putString("groupKey", "challenge_" + level);
        args.putString("difficulty", String.valueOf(dayIndex));
        args.putString("groupName", "Ngày " + dayIndex + " - " + repository.getDayTypeName(dayIndex));
        navController.navigate(R.id.action_challengeDay_to_player, args);
    }

    @Override
    public void onDestroyView() {
        if (rvExercises != null) {
            rvExercises.setAdapter(null);
        }
        super.onDestroyView();
    }
}
