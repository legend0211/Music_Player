package com.example.music_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FavouritesActivity extends Activity {
    ArrayList<Song> songDetails;
    ImageView backButton;
    ListView favouritesSongsListView;
    TextView songPresence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        initialisation();
        clickables();

        if(MainActivity.favouritesSongName.size()==0) {
            songPresence.setVisibility(View.VISIBLE);
            favouritesSongsListView.setVisibility(View.INVISIBLE);
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.favouritesSongName);
            favouritesSongsListView.setAdapter(adapter);
            favouritesSongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
        favouritesSongsListView = findViewById(R.id.favouritesSongsListView);
        songPresence = findViewById(R.id.songListPresence);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateListView() {
        if (MainActivity.favouritesSongDetails.size() == 0) {
            songPresence.setVisibility(View.VISIBLE);
            favouritesSongsListView.setVisibility(View.INVISIBLE);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.favouritesSongName);
            favouritesSongsListView.setAdapter(adapter);
            favouritesSongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }
}

