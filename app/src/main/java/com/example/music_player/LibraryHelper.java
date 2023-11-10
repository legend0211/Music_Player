package com.example.music_player;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = LibrarySong.class, exportSchema = false, version = 1)
public abstract class LibraryHelper extends RoomDatabase {
    private static final String DB_NAME = "libraryDB";
    private static LibraryHelper instance;

    public static synchronized LibraryHelper getDB(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context, LibraryHelper.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries().build();
        }
        return instance;
    }

    public abstract LibrarySongDao librarySongDao();
}
