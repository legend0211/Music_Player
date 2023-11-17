package com.example.music_player;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
@Entity(tableName = "library")
public class LibrarySong {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "path")
    private String songPath;

    @ColumnInfo(name = "time")
    private long time;

    LibrarySong(long id, String songPath, long time) {
        this.songPath = songPath;
        this.id = id;
        this.time = time;
    }

    @Ignore
    LibrarySong(long id, String songPath) {
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
    public long getId() {
        return id;
    }
    public String getSongPath() {
        return songPath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}