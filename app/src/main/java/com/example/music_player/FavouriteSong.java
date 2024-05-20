package com.example.music_player;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favourites")
public class FavouriteSong {
//    @PrimaryKey(autoGenerate = true)
//    private int id;
//    @ColumnInfo(name = "path")
//    private String songPath;

    @NonNull
    @PrimaryKey
    private String songPath;

//    FavouriteSong(int id, String songPath) {
//        this.songPath = songPath;
//        this.id = id;
//    }

    FavouriteSong(String songPath) {
        this.songPath = songPath;
    }
//    public void setId(int id) {
//        this.id = id;
//    }
//    public int getId() {
//        return id;
//    }
    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }
//    public void setPath(int id, String songPath) {
//        this.songPath = songPath;
//        this.id = id;
//    }
    public void setPath(String songPath) {
        this.songPath = songPath;
    }
    public String getSongPath() {
        return songPath;
    }

}