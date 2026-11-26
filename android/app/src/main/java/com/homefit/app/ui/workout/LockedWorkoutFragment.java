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
import com.homefit.app.util.LocalizationHelper;

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
        TextView tvIntroHeader = view.findViewById(R.id.tvIntroHeader);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
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

        tvTitle.setText(LocalizationHelper.localizeWorkoutTitle(requireContext(), title));
        if (tvDifficulty != null) {
            tvDifficulty.setText(LocalizationHelper.getLocalizedDifficulty(requireContext(), difficulty));
        }

        int mins = 17;
        try {
            if (duration != null && !duration.isEmpty()) {
                mins = Integer.parseInt(duration.replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {}
        tvDuration.setText(LocalizationHelper.localizeSubtitle(requireContext(), mins, difficulty).split("•")[0].trim());

        int exerciseCount = 13;
        try {
            if (count != null && !count.isEmpty()) {
                exerciseCount = Integer.parseInt(count.replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {}
        String lang = LocalizationHelper.getLanguage(requireContext());
        String localizedCount;
        switch (lang) {
            case "en": localizedCount = exerciseCount + " Exercises"; break;
            case "ko": localizedCount = exerciseCount + " 운동"; break;
            case "ja": localizedCount = exerciseCount + " エクササイズ"; break;
            default: localizedCount = exerciseCount + " Bài tập"; break;
        }
        tvCount.setText(localizedCount);

        if (tvIntroHeader != null) {
            tvIntroHeader.setText(R.string.workout_introduction);
        }

        if (tvDescription != null) {
            tvDescription.setText(LocalizationHelper.getLocalizedWorkoutDescription(requireContext(), groupKey, title));
        }

        if (btnUnlock != null) {
            btnUnlock.setText(R.string.workout_unlock);
        }

        if (coverImageUrl == null || coverImageUrl.isBlank() || coverImageUrl.contains("unsplash.com")) {
            coverImageUrl = getMuscleCoverUrlForGroup(groupKey, title);
        }

        if (ivHeaderImage != null) {
            String fullUrl = MediaLoader.resolveUrl(coverImageUrl);
            Glide.with(ivHeaderImage)
                    .load(fullUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .fitCenter()
                    .placeholder(R.drawable.bg_media_placeholder)
                    .error(R.drawable.ic_workout_placeholder)
                    .into(ivHeaderImage);
        }

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        if (btnUnlock != null) {
            btnUnlock.setOnClickListener(v -> {
                NavHostFragment.findNavController(this).navigate(R.id.action_lockedWorkout_to_proPaywall);
            });
        }
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
