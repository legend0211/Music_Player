package com.example.music_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class FavouritesActivity extends Activity {
    static ArrayList<Song> songDetails;
    static ImageView backButton;
    static ListView favouritesSongsListView;
    static TextView songPresence;
    static ImageView play_pauseButton;
    static TextView textTitle;
    static ConstraintLayout miniPlayerLayout;
    Handler handler = new Handler();
    static FrameLayout frameLayout;
    static FavouriteHelper favouriteHelper;


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
                    textTitle.setText(SongActivity.currentSong.name);
                    frameLayout.setVisibility(View.VISIBLE);
                }
                if(SongActivity.mediaPlayer!=null) {
                    if (!SongActivity.mediaPlayer.isPlaying()) {
                        play_pauseButton.setImageResource(R.drawable.ic_play);
                    } else {
                        play_pauseButton.setImageResource(R.drawable.ic_pause);
                    }
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
        textTitle = findViewById(R.id.miniTextTitle);
        play_pauseButton = findViewById(R.id.miniPlayButton);
        miniPlayerLayout = findViewById(R.id.miniPlayer);

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
        miniPlayerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavouritesActivity.this, SongActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("flag", 1);
                intent.putExtra("loop", SongActivity.loopToggler);
                startActivity(intent);
            }
        });
        play_pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.out.println("play_pauseButton Clicked");
                if (SongActivity.mediaPlayer.isPlaying()) {
                    SongActivity.mediaPlayer.pause();
                    SongActivity.duration = SongActivity.mediaPlayer.getCurrentPosition();
                    play_pauseButton.setImageResource(R.drawable.ic_play);
                } else {
                    SongActivity.mediaPlayer.start();
                    SongActivity.mediaPlayer.seekTo(SongActivity.duration);
                    play_pauseButton.setImageResource(R.drawable.ic_pause);
                    SongActivity.updateSeekBar();
                }
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
        favouriteHelper.favouriteSongDao().delete(new FavouriteSong(MainActivity.favouritesSongDetails.get(position).id, MainActivity.favouritesSongDetails.get(position).path));
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

