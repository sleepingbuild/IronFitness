package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.repository.ChallengeRepository;
import com.homefit.app.ui.common.MediaLoader;
import com.homefit.app.ui.library.ExerciseListAdapter;

import java.util.Locale;

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
    private ImageView ivHeaderPhoto;
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
        ivHeaderPhoto = view.findViewById(R.id.ivHeaderPhoto);
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
        if (!isAdded() || getContext() == null) return;
        final android.content.Context appContext = requireContext().getApplicationContext();

        com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
            com.homefit.app.data.local.AppDatabase db = com.homefit.app.data.local.AppDatabase.getInstance(appContext);
            var allWorkouts = db.workoutDao().getAllNow();
            com.homefit.app.data.local.entity.WorkoutEntity matched = null;
            if (allWorkouts != null) {
                String lvlVi = level.contains("easy") ? "dễ" : (level.contains("hard") ? "khó" : "trung bình");
                for (var w : allWorkouts) {
                    if (w == null) continue;
                    String wTitle = w.titleVi != null ? w.titleVi.toLowerCase(Locale.US) : "";
                    String wLevel = w.level != null ? w.level : "";
                    if (wTitle.contains("ngày " + dayIndex) && (wTitle.contains(lvlVi) || wLevel.equalsIgnoreCase(level))) {
                        matched = w;
                        break;
                    }
                }
            }

            java.util.List<LocalExerciseGroupDto.LocalWorkoutDto> syncedList = null;
            if (matched != null) {
                var items = db.workoutItemDao().getByWorkoutIdNow(matched.id);
                if (items != null && !items.isEmpty()) {
                    syncedList = new java.util.ArrayList<>();
                    for (var item : items) {
                        if (item == null) continue;
                        LocalExerciseGroupDto.LocalWorkoutDto dto = new LocalExerciseGroupDto.LocalWorkoutDto();
                        dto.id = String.valueOf(item.exerciseId);
                        dto.name = item.exerciseName != null ? item.exerciseName : "";
                        dto.mediaUrl = item.exerciseMediaUrl != null ? item.exerciseMediaUrl : "";
                        dto.reps_sets = "time".equalsIgnoreCase(item.measure) ? item.value + "s" : item.value + " lần";
                        dto.target = "Nghỉ " + item.restAfterSec + "s";
                        syncedList.add(dto);
                    }
                }
            }

            final java.util.List<LocalExerciseGroupDto.LocalWorkoutDto> finalSynced = syncedList;

            com.homefit.app.util.AppExecutors.mainThread().execute(() -> {
                if (!isAdded() || getContext() == null) return;
                currentSession = repository.getSessionForDay(level, dayIndex);
                if (currentSession != null) {
                    tvDayTitle.setText("NGÀY " + dayIndex);
                    String dayType = repository.getDayTypeName(dayIndex);
                    tvDaySubtitle.setText(dayType + " - " + repository.getLevelTitleVi(level));

                    if (finalSynced != null && !finalSynced.isEmpty()) {
                        currentSession.workouts = finalSynced;
                    }

                    tvDuration.setText(currentSession.duration_min + " phút");
                    int count = currentSession.workouts != null ? currentSession.workouts.size() : 0;
                    tvCount.setText(count + " Bài tập");

                    // Load High Quality Banner Photo
                    if (ivHeaderPhoto != null) {
                        String coverUrl = currentSession.coverImageUrl;
                        if (coverUrl == null || coverUrl.trim().isEmpty() || coverUrl.contains("unsplash.com")) {
                            coverUrl = getMuscleCoverUrlForGroup(currentSession.group, dayType);
                        }
                        String fullUrl = MediaLoader.resolveUrl(coverUrl);

                        Glide.with(this)
                                .load(fullUrl)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                .fitCenter()
                                .placeholder(R.drawable.bg_home_banner)
                                .error(R.drawable.ic_workout_placeholder)
                                .into(ivHeaderPhoto);
                    }

                    adapter.submitList(currentSession.workouts);
                }
            });
        });
    }

    private String getMuscleCoverUrlForGroup(String groupKey, String groupName) {
        String base = "https://ironfitness-et3g.onrender.com/images/muscles/";
        String combined = (groupKey != null ? groupKey : "") + " " + (groupName != null ? groupName : "");
        String g = combined.toLowerCase(Locale.US);
        if (g.contains("bung") || g.contains("abs") || g.contains("bụng")) return base + "abs.png";
        if (g.contains("nguc") || g.contains("chest") || g.contains("ngực")) return base + "chest.png";
        if (g.contains("tay") || g.contains("arms")) return base + "arms.png";
        if (g.contains("chan") || g.contains("legs") || g.contains("chân") || g.contains("mông")) return base + "legs.png";
        if (g.contains("back") || g.contains("lung") || g.contains("lưng")) return base + "back.png";
        return base + "fullbody.png";
    }

    private void startWorkoutPlayer() {
        if (currentSession == null) {
            currentSession = repository.getSessionForDay(level, dayIndex);
        }
        if (currentSession != null && currentSession.workouts != null && !currentSession.workouts.isEmpty()) {
            com.homefit.app.ui.player.ActiveWorkoutHolder.getInstance().setActiveWorkouts(currentSession.workouts);
        }
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
