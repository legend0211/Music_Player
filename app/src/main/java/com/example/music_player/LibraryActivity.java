package com.example.music_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.util.ArrayList;

public class LibraryActivity extends Activity {
    ArrayList<Song> songDetails;
    ImageView backButton;
    ListView librarySongsListView;
    TextView songPresence;
    ImageView play_pauseButton;
    TextView textTitle;
    ConstraintLayout miniPlayerLayout;
    Handler handler = new Handler();
    static FrameLayout frameLayout;
    static LibraryHelper libraryHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        initialisation();
        clickables();

        if(MainActivity.librarySongName.size()==0) {
            songPresence.setVisibility(View.VISIBLE);
            librarySongsListView.setVisibility(View.INVISIBLE);
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.librarySongName);
            librarySongsListView.setAdapter(adapter);
            librarySongsListView.setVisibility(View.VISIBLE);
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
                updateListView();
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
        librarySongsListView = findViewById(R.id.librarySongsListView);
        songPresence = findViewById(R.id.songListPresence);

        frameLayout = findViewById(R.id.miniPlayerContainer);
        textTitle = findViewById(R.id.miniTextTitle);
        play_pauseButton = findViewById(R.id.miniPlayButton);
        miniPlayerLayout = findViewById(R.id.miniPlayer);

        libraryHelper = LibraryHelper.getDB(this);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        librarySongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(LibraryActivity.this, SongActivity.class);
                intent.putExtra("song", MainActivity.librarySongDetails.get(position));
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        librarySongsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showOptionsDialog(i);
                return true;
            }
        });
        miniPlayerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryActivity.this, SongActivity.class);
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
        MediaPlayer m = new MediaPlayer();
        try {
            m.setDataSource(MainActivity.librarySongDetails.get(position).path);
            m.prepare();
            libraryHelper.librarySongDao().delete(new LibrarySong(MainActivity.librarySongDetails.get(position).id, MainActivity.librarySongDetails.get(position).path));
        }
        catch (IOException e) {}
        MainActivity.librarySongDetails.remove(position);
        MainActivity.librarySongName.remove(position);
        Toast.makeText(getApplicationContext(), "Song Removed", Toast.LENGTH_SHORT).show();
        updateListView();
    }

    private void updateListView() {
        if (MainActivity.librarySongName.size() == 0) {
            songPresence.setVisibility(View.VISIBLE);
            librarySongsListView.setVisibility(View.INVISIBLE);
        } else {
            ListViewAdapter adapter = new ListViewAdapter(this, MainActivity.librarySongName);
            librarySongsListView.setAdapter(adapter);
            librarySongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }
}
