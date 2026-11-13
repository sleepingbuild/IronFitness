package com.homefit.app.ui.common;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.homefit.app.R;

/**
 * Tải ảnh/GIF minh họa bằng Glide. Video (mediaType = VIDEO) chưa phát được trong danh sách
 * nên hiển thị biểu tượng video; ExoPlayer sẽ phát trong màn tập.
 */
public final class MediaLoader {

    private MediaLoader() {
    }

    public static boolean isVideo(@NonNull String mediaType) {
        return "VIDEO".equalsIgnoreCase(mediaType);
    }

    public static String resolveUrl(@NonNull String url) {
        String trimmed = url.trim();
        if (trimmed.startsWith("/")) {
            return "https://ironfitness-et3g.onrender.com" + trimmed;
        }
        return trimmed;
    }

    /** Ảnh nhỏ cho danh sách: chỉ lấy khung đầu của GIF để cuộn mượt. */
    public static void loadThumbnail(@NonNull ImageView target, @NonNull String url, @NonNull String mediaType) {
        Glide.with(target).clear(target);
        if (isVideo(mediaType)) {
            target.setImageResource(R.drawable.ic_media_video);
            return;
        }
        String fullUrl = resolveUrl(url);
        if (fullUrl.isEmpty()) {
            target.setImageResource(R.drawable.bg_media_placeholder);
            return;
        }
        Glide.with(target)
                .asBitmap()
                .load(fullUrl)
                .placeholder(R.drawable.bg_media_placeholder)
                .error(R.drawable.bg_media_placeholder)
                .centerCrop()
                .into(target);
    }

    /** Ảnh lớn cho màn chi tiết: GIF được phát. */
    public static void loadFull(@NonNull ImageView target, @NonNull String url, @NonNull String mediaType) {
        Glide.with(target).clear(target);
        if (isVideo(mediaType)) {
            target.setImageResource(R.drawable.ic_media_video);
            return;
        }
        String fullUrl = resolveUrl(url);
        if (fullUrl.isEmpty()) {
            target.setImageResource(R.drawable.bg_media_placeholder);
            return;
        }
        Glide.with(target)
                .load(fullUrl)
                .placeholder(R.drawable.bg_media_placeholder)
                .error(R.drawable.bg_media_placeholder)
                .fitCenter()
                .into(target);
    }
}
