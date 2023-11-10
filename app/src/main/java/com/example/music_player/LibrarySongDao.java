package com.example.music_player;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
@Dao
public interface LibrarySongDao {
    @Query("select * from library")
    List<LibrarySong> getAllLibrarySongs();
    @Insert
    void insert(LibrarySong librarySong);
    @Delete
    void delete(LibrarySong librarySong);

}

