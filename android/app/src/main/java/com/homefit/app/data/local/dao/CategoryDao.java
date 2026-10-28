package com.homefit.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.homefit.app.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sortOrder, id")
    LiveData<List<CategoryEntity>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<CategoryEntity> list);

    /** Xóa các danh mục server đã tắt (isActive = false). */
    @Query("DELETE FROM categories WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);

    /** Dùng sau lần đồng bộ đầy đủ: xóa mọi bản ghi không còn trong danh sách. Không gọi với list rỗng. */
    @Query("DELETE FROM categories WHERE id NOT IN (:ids)")
    void deleteMissing(List<Long> ids);
}
