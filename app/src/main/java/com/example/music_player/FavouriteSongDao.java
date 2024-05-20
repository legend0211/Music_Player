package com.example.music_player;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
@Dao
public interface FavouriteSongDao {
    @Query("select * from favourites")
    List<FavouriteSong> getAllFavoriteSongs();
    @Insert
    void insert(FavouriteSong favoriteSong);
    @Delete
    void delete(FavouriteSong favoriteSong);
}

