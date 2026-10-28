package com.homefit.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.homefit.app.data.local.entity.UserProfileEntity;

@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    LiveData<UserProfileEntity> get();

    /** Đồng bộ, chỉ gọi ngoài main thread. */
    @Query("SELECT * FROM user_profile WHERE id = 1")
    UserProfileEntity getNow();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserProfileEntity profile);

    @Query("DELETE FROM user_profile")
    void clear();
}
