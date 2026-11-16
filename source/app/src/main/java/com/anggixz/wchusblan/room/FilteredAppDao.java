package com.anggixz.wchusblan.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FilteredAppDao {
    @Insert
    void insert(FilteredApp app);

    @Insert
    void insertAll(List<FilteredApp> apps);

    @Delete
    void delete(FilteredApp app);

    @Query("DELETE FROM filtered_app")
    void deleteAll(); // 移除所有过滤的 App

    @Query("DELETE FROM filtered_app WHERE packageName = :pkg")
    void deleteByPackageName(String pkg);

    @Query("SELECT * FROM filtered_app")
    List<FilteredApp> getAll();

    @Query("SELECT EXISTS(SELECT 1 FROM filtered_app WHERE packageName = :pkg)")
    boolean isAppFiltered(String pkg);
}
