package com.homefit.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.homefit.app.data.local.entity.WorkoutLogEntity;

import java.util.List;

/** Mọi truy vấn đều lọc theo userId. */
@Dao
public interface WorkoutLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(WorkoutLogEntity log);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<WorkoutLogEntity> logs);

    @Query("SELECT * FROM workout_logs WHERE userId = :userId AND synced = 0 "
            + "ORDER BY completedAt LIMIT :limit")
    List<WorkoutLogEntity> getUnsynced(long userId, int limit);

    @Query("UPDATE workout_logs SET synced = 1 WHERE clientUuid IN (:uuids)")
    void markSynced(List<String> uuids);

    @Query("SELECT * FROM workout_logs WHERE userId = :userId "
            + "AND completedAt BETWEEN :from AND :to ORDER BY completedAt DESC")
    LiveData<List<WorkoutLogEntity>> getByMonth(long userId, long from, long to);

    @Query("SELECT COUNT(*) FROM workout_logs WHERE userId = :userId")
    LiveData<Integer> countAll(long userId);

    @Query("SELECT COALESCE(SUM(totalSeconds), 0) FROM workout_logs WHERE userId = :userId")
    LiveData<Integer> sumSeconds(long userId);

    @Query("SELECT COUNT(*) FROM workout_logs")
    int getCountNow();

    @Query("SELECT COALESCE(SUM(totalSeconds), 0) FROM workout_logs")
    int getTotalSecondsNow();

    @Query("SELECT completedAt FROM workout_logs ORDER BY completedAt DESC")
    List<Long> getAllCompletedTimesNow();

    @Query("SELECT completedAt FROM workout_logs WHERE userId = :userId "
            + "AND completedAt BETWEEN :from AND :to ORDER BY completedAt")
    LiveData<List<Long>> getDatesWithWorkout(long userId, long from, long to);
}
