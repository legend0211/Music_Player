package com.example.music_player;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {
    static ArrayList<Song> songDetails;
    static ImageView backButton;
    static ListView favouritesSongsListView;
    static TextView songPresence;
    Handler handler = new Handler();
    static FrameLayout frameLayout;
    static FavouriteHelper favouriteHelper;
    static ProgressBar progressBar;
    static RelativeLayout progressBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        initialisation();
        clickables();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.miniPlayerContainer, new MiniPlayerActivity()).commit();

        if(MainActivity.favouritesSongName.size()==0) {
            songPresence.setVisibility(View.VISIBLE);
            favouritesSongsListView.setVisibility(View.INVISIBLE);
        }
        else {
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.favouritesSongName);
            ListViewAdapter adapter = new ListViewAdapter(this, MainActivity.favouritesSongName);
            favouritesSongsListView.setAdapter(adapter);
            favouritesSongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(SongActivity.currentSong == null) {
                    frameLayout.setVisibility(View.GONE);
                }
                else {
                    frameLayout.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
        favouritesSongsListView = findViewById(R.id.favouritesSongsListView);
        songPresence = findViewById(R.id.songListPresence);

        frameLayout = findViewById(R.id.miniPlayerContainer);
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressBarLayout);

        favouriteHelper = FavouriteHelper.getDB(this);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        favouritesSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                progressBarLayout.setVisibility(RelativeLayout.VISIBLE);
                Intent intent = new Intent(FavouritesActivity.this, SongActivity.class);
                intent.putExtra("song", MainActivity.favouritesSongDetails.get(position));
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        favouritesSongsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showOptionsDialog(i);
                return true;
            }
        });
    }

    private void showOptionsDialog(int position) {
        final String[] options = {"Remove"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    removeItem(position);
                    break;
            }
        });
        builder.create().show();
    }

    private void removeItem(int position) {
//        favouriteHelper.favouriteSongDao().delete(new FavouriteSong(MainActivity.favouritesSongDetails.get(position).id, MainActivity.favouritesSongDetails.get(position).path));
        favouriteHelper.favouriteSongDao().delete(new FavouriteSong(MainActivity.favouritesSongDetails.get(position).path));
        MainActivity.favouritesSongDetails.get(position).favourites = false;
        MainActivity.favouritesSongDetails.remove(position);
        MainActivity.favouritesSongName.remove(position);
        Toast.makeText(getApplicationContext(), "Song Removed", Toast.LENGTH_SHORT).show();
        updateListView();
    }

    public void updateListView() {
        if (MainActivity.favouritesSongDetails.size() == 0) {
            songPresence.setVisibility(View.VISIBLE);
            favouritesSongsListView.setVisibility(View.INVISIBLE);
        } else {
            ListViewAdapter adapter = new ListViewAdapter(this, MainActivity.favouritesSongName);
            favouritesSongsListView.setAdapter(adapter);
            favouritesSongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
    }

}

