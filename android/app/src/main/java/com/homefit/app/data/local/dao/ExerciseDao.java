package com.homefit.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.homefit.app.data.local.entity.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM exercises WHERE categoryId = :categoryId ORDER BY sortOrder, id")
    LiveData<List<ExerciseEntity>> getByCategory(long categoryId);

    /** Đồng bộ, chỉ gọi ngoài main thread (Player nạp danh sách bài của một nhóm). */
    @Query("SELECT * FROM exercises WHERE categoryId = :categoryId ORDER BY sortOrder, id")
    List<ExerciseEntity> getByCategoryNow(long categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ExerciseEntity> list);

    /** Xóa các bài server đã tắt (isActive = false). */
    @Query("DELETE FROM exercises WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);

    /** Dùng sau lần đồng bộ đầy đủ. Không gọi với list rỗng. */
    @Query("DELETE FROM exercises WHERE id NOT IN (:ids)")
    void deleteMissing(List<Long> ids);
}
