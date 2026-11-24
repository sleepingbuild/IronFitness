package com.homefit.app.ui.common;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.homefit.app.R;

import java.util.Locale;

/**
 * Tải ảnh/GIF minh họa bằng Glide. Video (mediaType = VIDEO) chưa phát được trong danh sách
 * nên hiển thị biểu tượng video; ExoPlayer sẽ phát trong màn tập.
 */
public final class MediaLoader {

    private static final String BASE_MEDIA_URL = "https://raw.githubusercontent.com/sleepingbuild/homefit-media/main/";

    private MediaLoader() {
    }

    public static boolean isVideo(@NonNull String mediaType) {
        return "VIDEO".equalsIgnoreCase(mediaType);
    }

    public static String resolveUrl(@Nullable String url) {
        if (url == null) return "";
        String trimmed = url.trim();
        if (trimmed.startsWith("/")) {
            return "https://ironfitness-et3g.onrender.com" + trimmed;
        }
        return trimmed;
    }

    /**
     * Trả về URL ảnh 2D từ GitHub repository homefit-media theo tên bài tập.
     */
    @NonNull
    public static String getFallback2dMediaUrl(@Nullable String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            return BASE_MEDIA_URL + "abs/crunches.png";
        }
        String n = exerciseName.toLowerCase(Locale.US).trim();

        // Warm-up & Stretching (MUST check before generic "tay", "curl", "biceps")
        if (n.contains("arm circle") || n.contains("xoay tay") || n.contains("xoay cánh tay")) {
            return BASE_MEDIA_URL + "warm_up/arm_circles.png";
        }
        if (n.contains("hip circle") || n.contains("xoay hông")) {
            return BASE_MEDIA_URL + "warm_up/hip_circles.png";
        }
        if (n.contains("leg swing") || n.contains("đu chân")) {
            return BASE_MEDIA_URL + "warm_up/leg_swings.png";
        }
        if (n.contains("inchworm") || n.contains("bò sâu")) {
            return BASE_MEDIA_URL + "warm_up/inchworm.png";
        }
        if (n.contains("quad stretch") || n.contains("đùi trước")) {
            return BASE_MEDIA_URL + "stretching/quad_stretch.png";
        }
        if (n.contains("hamstring stretch") || n.contains("đùi sau")) {
            return BASE_MEDIA_URL + "stretching/hamstring_stretch.png";
        }
        if (n.contains("glute stretch") || n.contains("figure-4") || n.contains("figure 4") || n.contains("giãn mông") || n.contains("mông")) {
            return BASE_MEDIA_URL + "stretching/glute_stretch.png";
        }
        if (n.contains("hip flexor") || n.contains("gân háng") || n.contains("háng")) {
            return BASE_MEDIA_URL + "stretching/hip_flexor_stretch.png";
        }
        if (n.contains("chest stretch") || n.contains("giãn ngực") || n.contains("chest opener")) {
            return BASE_MEDIA_URL + "stretching/chest_stretch.png";
        }
        if (n.contains("shoulder stretch") || n.contains("giãn vai")) {
            return BASE_MEDIA_URL + "stretching/shoulder_stretch.png";
        }
        if (n.contains("child") || n.contains("em bé")) {
            return BASE_MEDIA_URL + "stretching/child_pose.png";
        }
        if (n.contains("cat-cow") || n.contains("cat cow") || n.contains("mèo bò") || n.contains("mèo - bò")) {
            return BASE_MEDIA_URL + "stretching/cat_cow_stretch.png";
        }
        if (n.contains("forward fold") || n.contains("gập người")) {
            return BASE_MEDIA_URL + "stretching/seated_forward_fold.png";
        }
        if (n.contains("spinal twist") || n.contains("xoay cột sống")) {
            return BASE_MEDIA_URL + "stretching/lying_spinal_twist.png";
        }
        if (n.contains("world") || n.contains("greatest stretch")) {
            return BASE_MEDIA_URL + "stretching/worlds_greatest_stretch.png";
        }
        if (n.contains("superman")) {
            return BASE_MEDIA_URL + "stretching/superman_stretch.png";
        }
        if (n.contains("bird dog") || n.contains("chó chim")) {
            return BASE_MEDIA_URL + "stretching/bird_dog.png";
        }
        if (n.contains("dead bug") || n.contains("bọ chết")) {
            return BASE_MEDIA_URL + "stretching/dead_bug.png";
        }

        // Specific Abs / Arm / Legs / Cardio
        if (n.contains("heel touch") || n.contains("heel tap") || n.contains("chạm gót")) {
            return BASE_MEDIA_URL + "abs/heel_touch.png";
        }
        if (n.contains("leg drop") || n.contains("leg raise") || n.contains("nâng chân")) {
            return BASE_MEDIA_URL + "abs/leg_raises.png";
        }
        if (n.contains("hollow body") || n.contains("hollow hold") || n.contains("l-sit hold") || n.contains("hollow")) {
            return BASE_MEDIA_URL + "abs/hollow_hold.png";
        }
        if (n.contains("windshield wiper") || n.contains("gạt nước")) {
            return BASE_MEDIA_URL + "abs/side_plank_upandown.png";
        }
        if (n.contains("bicycle") || n.contains("đạp xe")) {
            return BASE_MEDIA_URL + "abs/bicycle_crunches.png";
        }
        if (n.contains("reverse crunch") || n.contains("gập bụng ngược")) {
            return BASE_MEDIA_URL + "abs/reverse_cruches.png";
        }
        if (n.contains("knee touch") || n.contains("chạm gối")) {
            return BASE_MEDIA_URL + "abs/knee_touch.png";
        }
        if (n.contains("toe touch") || n.contains("chạm mũi chân")) {
            return BASE_MEDIA_URL + "abs/toe_touches.png";
        }
        if (n.contains("v-up") || n.contains("v crunch")) {
            return BASE_MEDIA_URL + "abs/v_crunches.png";
        }
        if (n.contains("crunch") || n.contains("gập bụng")) {
            return BASE_MEDIA_URL + "abs/crunches.png";
        }
        if (n.contains("side plank") || n.contains("plank nghiêng")) {
            return BASE_MEDIA_URL + "abs/side_plank.png";
        }
        if (n.contains("knee plank") || n.contains("plank quỳ")) {
            return BASE_MEDIA_URL + "abs/knee_plank.png";
        }
        if (n.contains("full arm plank") || n.contains("plank cao") || n.contains("plank thẳng")) {
            return BASE_MEDIA_URL + "abs/full_arm_plank.png";
        }
        if (n.contains("plank")) {
            return BASE_MEDIA_URL + "abs/plank.png";
        }
        if (n.contains("wall push") || n.contains("vào tường")) {
            return BASE_MEDIA_URL + "arm/wall_pushup.png";
        }
        if (n.contains("knee push") || n.contains("quỳ gối")) {
            return BASE_MEDIA_URL + "arm/knee_pushup.png";
        }
        if (n.contains("diamond") || n.contains("kim cương")) {
            return BASE_MEDIA_URL + "arm/diamond_pushup.png";
        }
        if (n.contains("pike")) {
            return BASE_MEDIA_URL + "arm/pike_pushp.png";
        }
        if (n.contains("pushup") || n.contains("push-up") || n.contains("chống đẩy") || n.contains("hít đất")) {
            return BASE_MEDIA_URL + "arm/pushup.png";
        }
        if (n.contains("dips") || n.contains("nhún tay") || n.contains("triceps")) {
            return BASE_MEDIA_URL + "arm/dips.png";
        }
        if (n.contains("shoulder tap") || n.contains("chạm vai")) {
            return BASE_MEDIA_URL + "arm/shoulder_tap.png";
        }
        if (n.contains("biceps") || n.contains("curl") || n.contains("tay trước") || n.contains("tay sau")) {
            return BASE_MEDIA_URL + "arm/biceps_curl.png";
        }
        if (n.contains("sumo squat")) {
            return BASE_MEDIA_URL + "legs/sumo_squat.png";
        }
        if (n.contains("jump squat")) {
            return BASE_MEDIA_URL + "legs/jump_squat.png";
        }
        if (n.contains("squat")) {
            return BASE_MEDIA_URL + "legs/squat.png";
        }
        if (n.contains("lunge") || n.contains("split squat") || n.contains("chùng chân")) {
            return BASE_MEDIA_URL + "legs/bulgarian_split_squat.png";
        }
        if (n.contains("calf") || n.contains("nhón gót")) {
            return BASE_MEDIA_URL + "legs/calf_raises.png";
        }
        if (n.contains("donkey")) {
            return BASE_MEDIA_URL + "legs/donkey_kick.png";
        }
        if (n.contains("glute") || n.contains("bridge") || n.contains("cầu mông")) {
            return BASE_MEDIA_URL + "legs/glute_bridge.png";
        }
        if (n.contains("wall sit")) {
            return BASE_MEDIA_URL + "legs/wall_sit.png";
        }
        if (n.contains("jumping jack") || n.contains("jack")) {
            return BASE_MEDIA_URL + "warm_up/jumping_jack.png";
        }
        if (n.contains("mountain climber") || n.contains("leo núi")) {
            return BASE_MEDIA_URL + "cardio/mountain_climber.png";
        }
        if (n.contains("burpee")) {
            return BASE_MEDIA_URL + "cardio/burpees.png";
        }

        return BASE_MEDIA_URL + "abs/crunches.png";
    }

    @NonNull
    public static String resolveExerciseMediaUrl(@Nullable String exerciseName, @Nullable String url) {
        if (url != null && !url.trim().isEmpty() && !url.contains("placeholder")) {
            return resolveUrl(url);
        }
        return getFallback2dMediaUrl(exerciseName);
    }

    /** Ảnh nhỏ cho danh sách: chỉ lấy khung đầu của GIF để cuộn mượt. */
    public static void loadThumbnail(@NonNull ImageView target, @Nullable String url, @Nullable String mediaType) {
        loadThumbnailWithName(target, null, url, mediaType);
    }

    public static void loadThumbnailWithName(@NonNull ImageView target, @Nullable String exerciseName, @Nullable String url, @Nullable String mediaType) {
        if (target == null || target.getContext() == null) return;
        try {
            Glide.with(target).clear(target);
            if (mediaType != null && isVideo(mediaType)) {
                target.setImageResource(R.drawable.ic_media_video);
                return;
            }
            String fullUrl = resolveExerciseMediaUrl(exerciseName, url);
            Glide.with(target)
                    .asBitmap()
                    .load(fullUrl)
                    .placeholder(R.drawable.bg_media_placeholder)
                    .error(Glide.with(target).asBitmap().load(getFallback2dMediaUrl(exerciseName)))
                    .fitCenter()
                    .into(target);
        } catch (Exception ignored) {}
    }

    /** Ảnh lớn cho màn chi tiết: GIF được phát. */
    public static void loadFull(@NonNull ImageView target, @Nullable String url, @Nullable String mediaType) {
        loadFullWithName(target, null, url, mediaType);
    }

    public static void loadFullWithName(@NonNull ImageView target, @Nullable String exerciseName, @Nullable String url, @Nullable String mediaType) {
        if (target == null || target.getContext() == null) return;
        try {
            Glide.with(target).clear(target);
            if (mediaType != null && isVideo(mediaType)) {
                target.setImageResource(R.drawable.ic_media_video);
                return;
            }
            String fullUrl = resolveExerciseMediaUrl(exerciseName, url);
            Glide.with(target)
                    .load(fullUrl)
                    .placeholder(R.drawable.bg_media_placeholder)
                    .error(Glide.with(target).load(getFallback2dMediaUrl(exerciseName)))
                    .fitCenter()
                    .into(target);
        } catch (Exception ignored) {}
    }
}
