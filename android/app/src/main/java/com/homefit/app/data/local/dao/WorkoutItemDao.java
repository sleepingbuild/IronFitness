package com.homefit.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.homefit.app.data.local.entity.WorkoutItemEntity;

import java.util.List;

@Dao
public interface WorkoutItemDao {
    @Query("SELECT * FROM workout_items WHERE workoutId = :workoutId ORDER BY position ASC")
    List<WorkoutItemEntity> getByWorkoutIdNow(long workoutId);

    @Query("SELECT * FROM workout_items WHERE workoutId = :workoutId ORDER BY position ASC")
    LiveData<List<WorkoutItemEntity>> getByWorkoutId(long workoutId);

    @Query("SELECT * FROM workout_items ORDER BY workoutId ASC, position ASC")
    List<WorkoutItemEntity> getAllNow();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<WorkoutItemEntity> items);

    @Query("DELETE FROM workout_items WHERE workoutId = :workoutId")
    void deleteByWorkoutId(long workoutId);

    @Query("DELETE FROM workout_items")
    void deleteAll();
}
