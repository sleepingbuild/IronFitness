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
                                if (dto.coverImageUrl == null || dto.coverImageUrl.trim().isEmpty()) {
                                    dto.coverImageUrl = getDefaultCoverForGroup(dto.group);
                                }
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

    private String getDefaultCoverForGroup(String group) {
        if (group == null) return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
        String g = group.toLowerCase();
        if (g.contains("bung")) return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800";
        if (g.contains("nguc")) return "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800";
        if (g.contains("tay")) return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800";
        if (g.contains("chan")) return "https://images.unsplash.com/photo-1434596922112-19c563067271?w=800";
        if (g.contains("back") || g.contains("lung")) return "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=800";
        if (g.contains("tabata")) return "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=800";
        if (g.contains("stretch")) return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800";
        return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
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
