package com.homefit.app.data.local;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AssetExerciseRepository {

    private static final String TAG = "AssetExerciseRepository";
    private final Context context;
    private final Gson gson = new Gson();

    private List<LocalExerciseGroupDto> cache = null;

    public AssetExerciseRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<LocalExerciseGroupDto> getAllGroups() {
        if (cache != null) return cache;
        cache = new ArrayList<>();
        try {
            String[] files = context.getAssets().list("exercises");
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".md") || file.endsWith(".json")) {
                        try (InputStreamReader reader = new InputStreamReader(
                                context.getAssets().open("exercises/" + file), StandardCharsets.UTF_8)) {
                            LocalExerciseGroupDto dto = gson.fromJson(reader, LocalExerciseGroupDto.class);
                            if (dto != null && dto.group != null) {
                                cache.add(dto);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc file: " + file, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đọc danh sách file assets/exercises", e);
        }
        return cache;
    }

    public List<LocalExerciseGroupDto.LocalWorkoutDto> getAllUniqueWorkouts() {
        List<LocalExerciseGroupDto> allGroups = getAllGroups();
        List<LocalExerciseGroupDto.LocalWorkoutDto> allWorkouts = new ArrayList<>();
        java.util.Set<String> addedIds = new java.util.HashSet<>();
        for (LocalExerciseGroupDto group : allGroups) {
            if (group.workouts != null) {
                for (LocalExerciseGroupDto.LocalWorkoutDto w : group.workouts) {
                    if (w.id != null && addedIds.add(w.id)) {
                        allWorkouts.add(w);
                    }
                }
            }
        }
        java.text.Collator collator = java.text.Collator.getInstance(new java.util.Locale("vi", "VN"));
        allWorkouts.sort((w1, w2) -> collator.compare(w1.name != null ? w1.name : "", w2.name != null ? w2.name : ""));
 return allWorkouts;
 }
}