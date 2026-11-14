package com.homefit.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.homefit.app.data.local.entity.WorkoutEntity;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY sortOrder ASC")
    LiveData<List<WorkoutEntity>> getAll();

    @Query("SELECT * FROM workouts WHERE type = :type ORDER BY sortOrder ASC")
    LiveData<List<WorkoutEntity>> getByType(String type);

    @Query("SELECT * FROM workouts WHERE id = :id")
    WorkoutEntity getByIdNow(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<WorkoutEntity> workouts);

    @Query("DELETE FROM workouts WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);

    @Query("DELETE FROM workouts")
    void deleteAll();
}
