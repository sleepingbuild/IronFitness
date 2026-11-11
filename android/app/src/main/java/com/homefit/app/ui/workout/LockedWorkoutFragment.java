package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;

public class LockedWorkoutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locked_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvCount = view.findViewById(R.id.tvCount);
        MaterialButton btnUnlock = view.findViewById(R.id.btnUnlock);

        String title = getArguments() != null ? getArguments().getString("title", "Bài tập PRO") : "Bài tập PRO";
        String duration = getArguments() != null ? getArguments().getString("duration", "17 phút") : "17 phút";
        String count = getArguments() != null ? getArguments().getString("count", "13 Bài tập") : "13 Bài tập";

        tvTitle.setText(title);
        tvDuration.setText(duration);
        tvCount.setText(count);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        btnUnlock.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_lockedWorkout_to_proPaywall);
        });
    }
}
