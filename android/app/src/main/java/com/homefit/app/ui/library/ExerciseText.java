package com.homefit.app.ui.library;

import android.content.Context;

import androidx.annotation.NonNull;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.ExerciseEntity;

/** Chuỗi mô tả bài tập: tập theo thời gian (giây) hoặc theo số lần, kèm thời gian nghỉ. */
public final class ExerciseText {

    private ExerciseText() {
    }

    /** "30 giây" hoặc "12 lần". */
    @NonNull
    public static String work(@NonNull Context context, @NonNull ExerciseEntity e) {
        if (e.durationSec != null) {
            return context.getString(R.string.exercise_work_duration, e.durationSec);
        }
        if (e.reps != null) {
            return context.getString(R.string.exercise_work_reps, e.reps);
        }
        return "";
    }

    /** "30 giây · Nghỉ 15 giây". */
    @NonNull
    public static String meta(@NonNull Context context, @NonNull ExerciseEntity e) {
        return context.getString(R.string.exercise_meta, work(context, e), e.restSec);
    }
}
