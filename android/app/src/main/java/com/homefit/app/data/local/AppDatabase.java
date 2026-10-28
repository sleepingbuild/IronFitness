package com.homefit.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.homefit.app.data.local.dao.CategoryDao;
import com.homefit.app.data.local.dao.ExerciseDao;
import com.homefit.app.data.local.dao.UserProfileDao;
import com.homefit.app.data.local.dao.WorkoutLogDao;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.local.entity.WorkoutLogEntity;

/**
 * Room database (version 1). Đổi schema phải tăng version và viết Migration;
 * KHÔNG dùng fallbackToDestructiveMigration ở bản release.
 */
@Database(
        entities = {
                CategoryEntity.class,
                ExerciseEntity.class,
                WorkoutLogEntity.class,
                UserProfileEntity.class
        },
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract CategoryDao categoryDao();

    public abstract ExerciseDao exerciseDao();

    public abstract WorkoutLogDao workoutLogDao();

    public abstract UserProfileDao userProfileDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(), AppDatabase.class, "homefit.db").build();
                }
            }
        }
        return instance;
    }
}
