package com.example.music_player;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = FavouriteSong.class, exportSchema = false, version = 1)
public abstract class FavouriteHelper extends RoomDatabase {
    private static final String DB_NAME = "favouritesDB";
    private static FavouriteHelper instance;

    public static synchronized FavouriteHelper getDB(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context, FavouriteHelper.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries().build();
        }
        return instance;
    }

    public abstract FavouriteSongDao favouriteSongDao();
}
