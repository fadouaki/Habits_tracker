package com.improveskill.habitstracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = habit.class,version = 1)
public abstract class HabitDataBase extends RoomDatabase {

    private static HabitDataBase instance ;
    public abstract habitDAO postDao();

    public static synchronized HabitDataBase getInstance(Context context) {
        if (instance==null){
            instance = Room.databaseBuilder(context, HabitDataBase.class,"database_habit")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
