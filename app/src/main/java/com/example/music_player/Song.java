package com.example.music_player;

import java.io.Serializable;

public class Song implements Serializable {
    String path;
    String name;
    String artist;
    boolean favourites = false;

}
