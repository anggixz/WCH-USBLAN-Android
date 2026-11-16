package com.anggixz.wchusblan;

import android.app.Application;

import androidx.room.Room;

import com.anggixz.wchusblan.room.AppDatabase;

public class MyApplication extends Application {
    private static MyApplication myApplication;
    private static AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        //初始化数据库
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "filter-db")
                .fallbackToDestructiveMigration()
                .build();

    }
    public static MyApplication getContext(){
        return myApplication;
    }

    public static AppDatabase getDb() {
        return db;
    }



}
