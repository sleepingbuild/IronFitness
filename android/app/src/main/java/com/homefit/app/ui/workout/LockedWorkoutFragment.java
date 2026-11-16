package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.ui.common.MediaLoader;

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
        ImageView ivHeaderImage = view.findViewById(R.id.ivHeaderImage);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDifficulty = view.findViewById(R.id.tvDifficulty);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvCount = view.findViewById(R.id.tvCount);
        MaterialButton btnUnlock = view.findViewById(R.id.btnUnlock);

        String title = "Bài tập PRO";
        String difficulty = "Trung bình";
        String duration = "17 phút";
        String count = "13 Bài tập";
        String coverImageUrl = null;
        String groupKey = "";

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString("title", args.getString("groupName", "Bài tập PRO"));
            difficulty = args.getString("difficulty", "Trung bình");
            duration = args.getString("duration", "17 phút");
            count = args.getString("count", "13 Bài tập");
            coverImageUrl = args.getString("coverImageUrl", null);
            groupKey = args.getString("groupKey", "");
        }

        tvTitle.setText(title);
        if (tvDifficulty != null) tvDifficulty.setText(difficulty);
        tvDuration.setText(duration);
        tvCount.setText(count);

        if (coverImageUrl == null || coverImageUrl.isBlank() || coverImageUrl.contains("unsplash.com")) {
            coverImageUrl = getMuscleCoverUrlForGroup(groupKey, title);
        }

        if (ivHeaderImage != null) {
            String fullUrl = MediaLoader.resolveUrl(coverImageUrl);
            Glide.with(ivHeaderImage)
                    .load(fullUrl)
                    .fitCenter()
                    .placeholder(R.drawable.bg_media_placeholder)
                    .into(ivHeaderImage);
        }

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        btnUnlock.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_lockedWorkout_to_proPaywall);
        });
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
}
