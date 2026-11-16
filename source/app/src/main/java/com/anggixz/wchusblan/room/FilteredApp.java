package com.anggixz.wchusblan.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "filtered_app")

public class FilteredApp {
    @PrimaryKey
    @NonNull
    public String packageName; // 唯一键，App 包名
}
