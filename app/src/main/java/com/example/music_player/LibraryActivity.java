package com.example.music_player;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity {
    ArrayList<Song> songDetails;
    static Intent intent;
    ImageView backButton;
    ConstraintLayout favButton;
    ListView librarySongsListView;
    TextView songPresence;
    Handler handler = new Handler();
    static FrameLayout frameLayout;
    static LibraryHelper libraryHelper;
    static ProgressBar progressBar;
    static RelativeLayout progressBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        initialisation();
        clickables();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.miniPlayerContainer, new MiniPlayerActivity()).commit();


        if(MainActivity.librarySongName.size()==0) {
            songPresence.setVisibility(View.VISIBLE);
            librarySongsListView.setVisibility(View.INVISIBLE);
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.librarySongName);
            librarySongsListView.setAdapter(adapter);
            librarySongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
            updateListView();
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
        librarySongsListView = findViewById(R.id.librarySongsListView);
        songPresence = findViewById(R.id.songListPresence);
        favButton = findViewById(R.id.constraintLayout3);

        frameLayout = findViewById(R.id.miniPlayerContainer);
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressBarLayout);

        libraryHelper = LibraryHelper.getDB(this);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LibraryActivity.this, FavouritesActivity.class);
                startActivity(intent);
            }
        });
        librarySongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                progressBarLayout.setVisibility(RelativeLayout.VISIBLE);
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
        libraryHelper.librarySongDao().delete(new LibrarySong(SongActivity.currentSong.id, MainActivity.librarySongDetails.get(position).path));
        MainActivity.librarySongDetails.remove(position);
        MainActivity.librarySongName.remove(position);
        Toast.makeText(getApplicationContext(), "Song Removed", Toast.LENGTH_SHORT).show();
        updateListView();
    }

    public void updateListView() {
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
