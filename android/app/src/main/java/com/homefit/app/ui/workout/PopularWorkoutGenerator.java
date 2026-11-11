package com.homefit.app.ui.workout;

import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;

import java.util.ArrayList;

public class PopularWorkoutGenerator {

    public static LocalExerciseGroupDto createFatLossEasy() {
        LocalExerciseGroupDto dto = createBaseDto("fatloss", "Đốt mỡ nhẹ nhàng", "Easy", "Circuit - 2 vòng (30s làm / 20s nghỉ)", 20);
        addWorkouts(dto, "Warm-up", "30s", "Arm Circles", "Hip Circles", "Bodyweight Squat chậm", "High Knees nhẹ", "Cat-Cow");
        addWorkouts(dto, "Main", "30s", "Bodyweight Squat", "Knee Push-up", "Reverse Lunge", "Glute Bridge", "Plank", "Mountain Climber chậm", "Jumping Jack nhẹ", "Crunches");
        addWorkouts(dto, "Cool-down", "30s", "Standing Quad Stretch", "Standing Hamstring Stretch", "Figure-4 Stretch", "Child's Pose", "Chest Opener", "Shoulder Cross-Body Stretch");
        return dto;
    }

    public static LocalExerciseGroupDto createFatLossMedium() {
        LocalExerciseGroupDto dto = createBaseDto("fatloss", "HIIT toàn thân", "Medium", "Circuit - 3 vòng (35s làm / 20s nghỉ)", 25);
        addWorkouts(dto, "Warm-up", "30s", "Jumping Jack", "Arm Circles", "Leg Swings", "Bodyweight Squat", "Inchworm", "High Knees");
        addWorkouts(dto, "Main", "35s", "Jump Squat", "Standard Push-up", "Reverse Lunge", "Mountain Climber", "Plank with Shoulder Tap", "Bicycle Crunch", "Superman", "Burpee");
        addWorkouts(dto, "Cool-down", "30s", "Kneeling Hip Flexor Stretch", "Seated Forward Fold", "Figure-4 Stretch", "Child's Pose", "Lying Spinal Twist", "Chest Opener");
        return dto;
    }

    public static LocalExerciseGroupDto createFatLossHard() {
        LocalExerciseGroupDto dto = createBaseDto("fatloss", "Đốt cháy calo toàn thân", "Hard", "Circuit - 3 vòng (40s làm / 15s nghỉ)", 28);
        addWorkouts(dto, "Warm-up", "30s", "Jumping Jack", "High Knees", "Butt Kicks", "Bodyweight Squat", "Inchworm", "World's Greatest Stretch", "Arm Circles");
        addWorkouts(dto, "Main", "40s", "Jump Squat", "Clap Push-up", "Jumping Lunge", "Mountain Climber nhanh", "Burpee full", "Plank Jack", "Bicycle Crunch nhanh", "Superman Pulse", "High Knees tăng tốc");
        addWorkouts(dto, "Cool-down", "30s", "Standing Quad Stretch", "Standing Hamstring Stretch", "Figure-4 Stretch", "Kneeling Hip Flexor Stretch", "Child's Pose", "Lying Spinal Twist", "Chest Opener + Shoulder Stretch");
        return dto;
    }

    public static LocalExerciseGroupDto createMuscleMedium1() {
        LocalExerciseGroupDto dto = createBaseDto("muscle", "Ngực & Tay", "Medium", "Straight sets - 3 hiệp mỗi bài", 22);
        addWorkouts(dto, "Warm-up", "30s", "Arm Circles", "Scapular Push-up nhẹ", "Incline Push-up", "Cat-Cow", "Shoulder Circles");
        addWorkouts(dto, "Main", "10-12 lần", "Standard Push-up", "Diamond Push-up", "Wide Push-up", "Chair Tricep Dips", "Pike Push-up", "Push-up with Pause", "Plank to Downward Dog");
        addWorkouts(dto, "Cool-down", "30s", "Chest Opener", "Shoulder Cross-Body Stretch", "Tricep Stretch", "Child's Pose", "Cat-Cow", "Standing Forward Fold");
        return dto;
    }

    public static LocalExerciseGroupDto createMuscleMedium2() {
        LocalExerciseGroupDto dto = createBaseDto("muscle", "Chân & Mông", "Medium", "Straight sets - 3 hiệp mỗi bài", 22);
        addWorkouts(dto, "Warm-up", "30s", "Bodyweight Squat chậm", "Hip Circles", "Leg Swings", "Glute Bridge", "Reverse Lunge nhẹ");
        addWorkouts(dto, "Main", "12-15 lần", "Bodyweight Squat", "Bulgarian Split Squat", "Reverse Lunge", "Single-leg Glute Bridge", "Sumo Squat", "Step-up", "Calf Raise");
        addWorkouts(dto, "Cool-down", "30s", "Standing Quad Stretch", "Standing Hamstring Stretch", "Figure-4 Stretch", "Kneeling Hip Flexor Stretch", "Seated Forward Fold", "Child's Pose");
        return dto;
    }

    public static LocalExerciseGroupDto createMuscleMedium3() {
        LocalExerciseGroupDto dto = createBaseDto("muscle", "Thân trên + Lõi", "Medium", "Straight sets + Circuit core", 22);
        addWorkouts(dto, "Warm-up", "30s", "Arm Circles", "Cat-Cow", "Scapular Push-up", "Dead Bug nhẹ", "Bird Dog");
        addWorkouts(dto, "Main", "10-12 lần", "Standard Push-up", "Superman", "Inverted Row", "Pike Push-up", "Plank", "Dead Bug", "Bicycle Crunch", "Bird Dog");
        addWorkouts(dto, "Cool-down", "30s", "Chest Opener", "Shoulder Stretch", "Child's Pose", "Lying Spinal Twist", "Cat-Cow", "Seated Forward Fold");
        return dto;
    }

    public static LocalExerciseGroupDto createKeepFitEasy() {
        LocalExerciseGroupDto dto = createBaseDto("keepfit", "Toàn thân cân bằng", "Easy", "Circuit - 2 vòng (30s làm / 20s nghỉ)", 15);
        addWorkouts(dto, "Warm-up", "30s", "Arm Circles", "Hip Circles", "Bodyweight Squat", "Cat-Cow");
        addWorkouts(dto, "Main", "30s", "Bodyweight Squat", "Knee Push-up", "Glute Bridge", "Bird Dog", "Plank", "Reverse Lunge", "Crunches");
        addWorkouts(dto, "Cool-down", "30s", "Standing Quad Stretch", "Figure-4 Stretch", "Child's Pose", "Chest Opener", "Shoulder Stretch");
        return dto;
    }

    public static LocalExerciseGroupDto createKeepFitMedium() {
        LocalExerciseGroupDto dto = createBaseDto("keepfit", "Toàn thân + Lõi", "Medium", "Circuit - 3 vòng (35s làm / 20s nghỉ)", 20);
        addWorkouts(dto, "Warm-up", "30s", "Jumping Jack", "Arm Circles", "Bodyweight Squat", "Inchworm", "Cat-Cow");
        addWorkouts(dto, "Main", "35s", "Squat", "Standard Push-up", "Reverse Lunge", "Superman", "Plank with Shoulder Tap", "Mountain Climber", "Bicycle Crunch", "Bird Dog");
        addWorkouts(dto, "Cool-down", "30s", "Standing Quad Stretch", "Standing Hamstring Stretch", "Figure-4 Stretch", "Child's Pose", "Lying Spinal Twist", "Chest Opener");
        return dto;
    }

    public static LocalExerciseGroupDto createKeepFitHard() {
        LocalExerciseGroupDto dto = createBaseDto("keepfit", "Sức bền toàn thân", "Hard", "Circuit - 3 vòng (40s làm / 15s nghỉ)", 25);
        addWorkouts(dto, "Warm-up", "30s", "Jumping Jack", "High Knees", "Butt Kicks", "Bodyweight Squat", "Inchworm", "World's Greatest Stretch");
        addWorkouts(dto, "Main", "40s", "Jump Squat", "Push-up", "Jumping Lunge", "Mountain Climber", "Burpee", "Plank Jack", "Superman Pulse", "Bicycle Crunch", "High Knees");
        addWorkouts(dto, "Cool-down", "30s", "Standing Quad Stretch", "Standing Hamstring Stretch", "Figure-4 Stretch", "Kneeling Hip Flexor Stretch", "Child's Pose", "Lying Spinal Twist", "Chest Opener + Shoulder Stretch");
        return dto;
    }

    private static LocalExerciseGroupDto createBaseDto(String group, String name, String difficulty, String desc, int durationMin) {
        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = group;
        dto.group_name = name;
        dto.difficulty = difficulty;
        dto.description = desc;
        dto.duration_min = durationMin;
        dto.workouts = new ArrayList<>();
        return dto;
    }

    private static void addWorkouts(LocalExerciseGroupDto dto, String target, String repsSets, String... names) {
        for (String name : names) {
            LocalWorkoutDto w = new LocalWorkoutDto();
            w.id = name.toLowerCase().replace(" ", "_").replace("-", "_");
            w.name = name;
            w.target = target;
            w.reps_sets = repsSets;
            w.instructions = new ArrayList<>();
            w.instructions.add("Thực hiện " + name + " theo đúng kỹ thuật.");
            w.tips = "Giữ nhịp thở đều.";
            dto.workouts.add(w);
        }
    }
}