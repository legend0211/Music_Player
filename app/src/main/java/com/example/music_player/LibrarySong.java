package com.example.music_player;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "library")
public class LibrarySong {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "path")
    private String songPath;

    LibrarySong(int id, String songPath) {
        this.songPath = songPath;
        this.id = id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }
    public void setPath(int id, String songPath) {
        this.songPath = songPath;
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public String getSongPath() {
        return songPath;
    }

}