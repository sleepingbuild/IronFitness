package com.homefit.app.ui.workout;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AssetExerciseRepository;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;

import java.util.ArrayList;
import java.util.List;

public class FilteredGroupsViewModel extends AndroidViewModel {

    private final AssetExerciseRepository repository;

    public FilteredGroupsViewModel(@NonNull Application application) {
        super(application);
        repository = new AssetExerciseRepository(application);
    }

    public LiveData<List<LocalExerciseGroupDto>> getFilteredGroups(String filterType) {
        MutableLiveData<List<LocalExerciseGroupDto>> liveData = new MutableLiveData<>();
        List<LocalExerciseGroupDto> all = repository.getAllGroups();
        List<LocalExerciseGroupDto> result = new ArrayList<>();

        if (all != null) {
            for (LocalExerciseGroupDto dto : all) {
                int count = dto.workouts != null ? dto.workouts.size() : 0;
                String diffStr = dto.difficulty != null ? dto.difficulty : "Medium";
                
                // Thuật toán tính tổng phút hiển thị từ HomeWorkoutAdapter.java
                int duration = 0;
                if (diffStr.equalsIgnoreCase("Easy")) duration = (int) (count * 1.0); // ~1 min per exercise
                else if (diffStr.equalsIgnoreCase("Medium")) duration = (int) (count * 1.5); // ~1.5 min per exercise
                else if (diffStr.equalsIgnoreCase("Hard")) duration = (int) (count * 2.0); // ~2 min per exercise

                switch (filterType) {
                    case "under_7":
                        if (duration > 0 && duration < 7) result.add(dto);
                        break;
                    case "7_to_15":
                        if (duration >= 7 && duration <= 15) result.add(dto);
                        break;
                    case "over_15":
                        if (duration > 15) result.add(dto);
                        break;
                    case "beginner":
                        if ("Easy".equalsIgnoreCase(dto.difficulty)) result.add(dto);
                        break;
                    case "warmup":
                        if ("khoi_dong".equalsIgnoreCase(dto.group)) result.add(dto);
                        break;
                    case "stretch":
                    case "short_workout":
                        if ("gian_co".equalsIgnoreCase(dto.group)) result.add(dto);
                        break;
                    case "calorie_burn":
                        // Will handle below
                        break;
                    case "leg_strength":
                        // Will handle below
                        break;
                    case "tabata":
                    case "stretch_back":
                        // Will handle below
                        break;
                    default:
                        result.add(dto); // Fallback to all
                        break;
                }
            }
        }
        
        if ("calorie_burn".equals(filterType)) {
            result.clear();
            result.add(createCalorieBurnWorkout("Easy"));
            result.add(createCalorieBurnWorkout("Medium"));
            result.add(createCalorieBurnWorkout("Hard"));
        } else if ("leg_strength".equals(filterType)) {
            result.clear();
            result.add(createLegChallengeWorkout("Trung bình", 1));
        } else if ("tabata".equals(filterType)) {
            result.clear();
            result.add(createTabataWorkout("Medium"));
        } else if ("stretch_back".equals(filterType)) {
            result.clear();
            result.add(createBackStretchWorkout());
        }

        liveData.setValue(result);
        return liveData;
    }

    private LocalExerciseGroupDto createTabataWorkout(String level) {
        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = "toan_than";
        dto.group_name = "6 phút với Tabata";
        dto.difficulty = level.equals("Trung bình") ? "Medium" : level;
        dto.description = "Buổi HIIT ngắn, nhịp độ cố định (20s làm / 10s nghỉ x 8 hiệp)";
        dto.duration_min = 6;
        dto.coverImageUrl = "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=800";
        dto.workouts = new ArrayList<>();
        
        // Block A: Khởi động
        dto.workouts.add(createWorkout("jumping_jack", "Jumping Jack", "30s", "Khởi động"));
        dto.workouts.add(createWorkout("torso_twist", "Xoay hông", "30s", "Khởi động"));
        
        // Block B: Tabata 8 hiệp (4 bài x 2)
        dto.workouts.add(createWorkout("squat_jump_low", "Squat nhảy thấp", "20s", "Thân dưới"));
        dto.workouts.add(createWorkout("push_up", "Push-up", "20s", "Thân trên"));
        dto.workouts.add(createWorkout("mountain_climber", "Mountain Climber", "20s", "Lõi"));
        dto.workouts.add(createWorkout("lunge_reverse", "Lunge lùi luân phiên", "20s", "Tim mạch"));
        dto.workouts.add(createWorkout("squat_jump_low", "Squat nhảy thấp", "20s", "Thân dưới"));
        dto.workouts.add(createWorkout("push_up", "Push-up", "20s", "Thân trên"));
        dto.workouts.add(createWorkout("mountain_climber", "Mountain Climber", "20s", "Lõi"));
        dto.workouts.add(createWorkout("lunge_reverse", "Lunge lùi luân phiên", "20s", "Tim mạch"));
        
        // Block C: Giãn cơ
        dto.workouts.add(createWorkout("quad_stretch", "Giãn đùi trước", "30s", "Giãn cơ"));
        dto.workouts.add(createWorkout("hamstring_stretch", "Giãn đùi sau", "30s", "Giãn cơ"));
        return dto;
    }

    private LocalExerciseGroupDto createBackStretchWorkout() {
        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = "gian_co";
        dto.group_name = "Kéo giãn lưng";
        dto.difficulty = "Easy";
        dto.description = "Giảm cứng và mỏi lưng, cải thiện linh hoạt (Giãn tĩnh)";
        dto.duration_min = 7;
        dto.coverImageUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800";
        dto.workouts = new ArrayList<>();
        
        dto.workouts.add(createWorkout("cat_cow", "Cat-Cow", "35s", "Vận động nhẹ"));
        dto.workouts.add(createWorkout("thread_needle", "Thread the Needle", "65s", "Lưng trên"));
        dto.workouts.add(createWorkout("child_pose", "Child's Pose", "35s", "Lưng dưới"));
        dto.workouts.add(createWorkout("sphinx_pose", "Sphinx/Cobra nhẹ", "35s", "Lưng dưới"));
        dto.workouts.add(createWorkout("knee_to_chest", "Ôm gối vào ngực", "35s", "Lưng dưới"));
        dto.workouts.add(createWorkout("supine_twist", "Xoay cột sống nằm ngửa", "65s", "Cột sống"));
        dto.workouts.add(createWorkout("figure_4", "Figure-4", "65s", "Hông và mông"));
        dto.workouts.add(createWorkout("relax_breathing", "Hít thở thư giãn", "60s", "Thư giãn"));
        return dto;
    }

    private LocalExerciseGroupDto createCalorieBurnWorkout(String level) {
        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = "toan_than";
        dto.group_name = "Đốt cháy calo toàn thân";
        dto.difficulty = level;
        dto.description = "HIIT toàn thân, nhịp tim cao, thiên về đốt năng lượng (3 vòng)";
        dto.coverImageUrl = "https://images.unsplash.com/photo-1549576490-b0b4831ef60a?w=800";
        dto.workouts = new ArrayList<>();

        if ("Hard".equalsIgnoreCase(level)) {
            dto.duration_min = 28;
            dto.workouts.add(createWorkout("jump_squat", "Jump Squat", "45s", "Tim mạch - thân dưới"));
            dto.workouts.add(createWorkout("push_up", "Push-up", "45s", "Đẩy - thân trên"));
            dto.workouts.add(createWorkout("mountain_climber", "Mountain Climber", "45s", "Lõi động"));
            dto.workouts.add(createWorkout("jump_lunge", "Jump Lunge", "45s", "Thân dưới đơn chân"));
            dto.workouts.add(createWorkout("shoulder_tap", "Plank chạm vai", "45s", "Thân sau - lõi"));
            dto.workouts.add(createWorkout("burpee", "Burpee", "45s", "Toàn thân đỉnh"));
            dto.workouts.add(createWorkout("high_knees", "High Knees", "45s", "Tim mạch kết thúc"));
        } else if ("Medium".equalsIgnoreCase(level)) {
            dto.duration_min = 25;
            dto.workouts.add(createWorkout("squat", "Squat nhanh", "40s", "Tim mạch - thân dưới"));
            dto.workouts.add(createWorkout("push_up_knee", "Push-up quỳ gối", "40s", "Đẩy - thân trên"));
            dto.workouts.add(createWorkout("mountain_climber", "Mountain Climber chậm", "40s", "Lõi động"));
            dto.workouts.add(createWorkout("lunge", "Reverse Lunge", "40s", "Thân dưới đơn chân"));
            dto.workouts.add(createWorkout("burpee_no_jump", "Burpee không nhảy", "40s", "Toàn thân đỉnh"));
            dto.workouts.add(createWorkout("high_knees", "High Knees", "40s", "Tim mạch kết thúc"));
        } else {
            dto.duration_min = 22;
            dto.workouts.add(createWorkout("squat_stand", "Squat to Stand", "30s", "Tim mạch - thân dưới"));
            dto.workouts.add(createWorkout("wall_push_up", "Push-up tường", "30s", "Đẩy - thân trên"));
            dto.workouts.add(createWorkout("standing_knee_elbow", "Nâng gối chạm khuỷu", "30s", "Lõi động"));
            dto.workouts.add(createWorkout("step_back", "Step-back Lunge nhẹ", "30s", "Thân dưới đơn chân"));
            dto.workouts.add(createWorkout("plank_knee", "Plank quỳ gối", "30s", "Thân sau - lõi"));
            dto.workouts.add(createWorkout("step_jack", "Step-out Jack", "30s", "Tim mạch kết thúc"));
        }
        return dto;
    }

    private LocalExerciseGroupDto createLegChallengeWorkout(String level, int week) {
        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = "chan";
        dto.group_name = "Thử thách sức mạnh chân Tuần " + week;
        dto.difficulty = level.equals("Trung bình") ? "Medium" : level;
        dto.description = "Tăng sức mạnh thân dưới bằng cách tăng dần độ khó (2 vòng x 7 bài)";
        dto.duration_min = 21;
        dto.coverImageUrl = "https://images.unsplash.com/photo-1434596922112-19c563067271?w=800";
        dto.workouts = new ArrayList<>();

        dto.workouts.add(createWorkout("squat_slow", "Squat chậm", "40s", "Nhịp xuống 3 giây"));
        dto.workouts.add(createWorkout("reverse_lunge", "Lunge lùi", "40s", "Mỗi chân 20s"));
        dto.workouts.add(createWorkout("glute_bridge", "Cầu mông (Glute Bridge)", "40s", "Siết mông 1 giây ở đỉnh"));
        dto.workouts.add(createWorkout("sumo_squat", "Sumo Squat", "40s", "Cơ khép, mông"));
        dto.workouts.add(createWorkout("single_rdl", "Single-leg RDL không tạ", "40s", "Mỗi chân 20s, giữ thăng bằng"));
        dto.workouts.add(createWorkout("calf_raise", "Nhón gót (Calf Raise)", "40s", "Cô lập bắp chân"));
        dto.workouts.add(createWorkout("wall_sit", "Wall Sit", "40s", "Bài kết thúc cho đến khi mỏi"));

        return dto;
    }

    private LocalExerciseGroupDto.LocalWorkoutDto createWorkout(String id, String name, String repsSets, String target) {
        LocalExerciseGroupDto.LocalWorkoutDto w = new LocalExerciseGroupDto.LocalWorkoutDto();
        w.id = id;
        w.name = name;
        w.reps_sets = repsSets;
        w.target = target;
        w.instructions = new ArrayList<>();
        w.instructions.add("Thực hiện " + name + " theo đúng kỹ thuật.");
        w.tips = "Giữ nhịp thở đều.";
        return w;
    }
}
