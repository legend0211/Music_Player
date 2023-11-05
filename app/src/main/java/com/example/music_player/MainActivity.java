package com.example.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {
    private final int READ_STORAGE_PERMISSION_REQUEST = 1;
    static Intent intent;
    static Handler handler;
    static TextView textTitle;
    static ConstraintLayout miniPlayerLayout;
    static ListView songListView;
    static ImageView searchButton, myQueueList, favButton, libraryButton, play_pauseButton;
    static FrameLayout frameLayout;
    static File[] songFolderFiles;
    static ArrayList<String> nameOfSongs;
    static ArrayList<Song> songDetails;
    static ArrayList<String> queueSongName = new ArrayList<>();
    static ArrayList<Song> queueSongDetails = new ArrayList<>();
    static ArrayList<String> librarySongName = new ArrayList<>();
    static ArrayList<Song> librarySongDetails = new ArrayList<>();
    static ArrayList<String> favouritesSongName = new ArrayList<>();
    static ArrayList<Song> favouritesSongDetails = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialisations();
        clickables();

        int currentApiVersion = Build.VERSION.SDK_INT;
        if(currentApiVersion>=33) {
            requestStoragePermissionForHigherVersions();
        }
        else {
            requestStoragePermission();
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

    @AfterPermissionGranted(READ_STORAGE_PERMISSION_REQUEST)
    public void requestStoragePermissionForHigherVersions() {
        String perms = Manifest.permission.READ_MEDIA_AUDIO;
        if(EasyPermissions.hasPermissions(this, perms)) {
            getSongs();
            System.out.println("Has storage permission");
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the storage permission", READ_STORAGE_PERMISSION_REQUEST, perms);
            System.out.println("Doesn't have storage permission");
        }
    }

    @AfterPermissionGranted(READ_STORAGE_PERMISSION_REQUEST)
    public void requestStoragePermission() {
        String perms = Manifest.permission.READ_EXTERNAL_STORAGE;
        if(EasyPermissions.hasPermissions(this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            System.out.println("Has storage permission");
            getSongs();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the storage permission", READ_STORAGE_PERMISSION_REQUEST, perms);
            System.out.println("Doesn't have storage permission");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now you can get the songs
                getSongs();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Cannot access songs.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void initialisations() {
        searchButton = findViewById(R.id.Search);
        songListView = findViewById(R.id.songsListView);
        myQueueList = findViewById(R.id.myQueueList);
        favButton = findViewById(R.id.favourites);
        libraryButton = findViewById(R.id.library);
        frameLayout = findViewById(R.id.miniPlayerContainer);

        nameOfSongs = new ArrayList<>();
        songDetails = new ArrayList<>();

        handler = new Handler();

        textTitle = findViewById(R.id.miniTextTitle);
        play_pauseButton = findViewById(R.id.miniPlayButton);
        miniPlayerLayout = findViewById(R.id.miniPlayer);
    }

    public void clickables() {
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Entered Song");
                String songName = (String) parent.getItemAtPosition(position);
                System.out.println(position);

                intent = new Intent(MainActivity.this, SongActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        songListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showOptionsDialog(i);
                return true;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("songList", songDetails);
                startActivity(intent);
            }
        });

        myQueueList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, MyQueueActivity.class);
                startActivity(intent);
            }
        });

        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(intent);
            }
        });

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, FavouritesActivity.class);
                startActivity(intent);
            }
        });

        miniPlayerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SongActivity.class);
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

    public void getSongs() {
        File songFolder = new File(Environment.getExternalStorageDirectory(), "SongFolder");
        songFolderFiles = songFolder.listFiles();
        System.out.println("Size = "+songFolderFiles.length);
        if (songFolderFiles != null) {
            for (File file : songFolderFiles) {
                nameOfSongs.add("  "+file.getName().substring(0,file.getName().length()-4));
                Song song = new Song();
                song.artist = "Anonymous";
                song.name = nameOfSongs.get(nameOfSongs.size()-1);
                song.path = file.getPath();
                songDetails.add(song);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, nameOfSongs);
        songListView.setAdapter(adapter);
    }

    private void showOptionsDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");

        final String[] options = {"Add to Beginning", "Add to End"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    addToQueueBeginning(position);
                    break;
                case 1:
                    addToQueueEnd(position);
                    break;
            }
        });
        builder.create().show();
    }

    private void addToQueueBeginning(int position) {
        queueSongName.add(0, songDetails.get(position).name);
        queueSongDetails.add(0, songDetails.get(position));
        Toast.makeText(getApplicationContext(), "Song Added to Queue Beginning", Toast.LENGTH_SHORT).show();
    }

    private void addToQueueEnd(int position) {
        queueSongName.add(songDetails.get(position).name);
        queueSongDetails.add(songDetails.get(position));
        Toast.makeText(getApplicationContext(), "Song Added to Queue End", Toast.LENGTH_SHORT).show();
    }
}
// some changes