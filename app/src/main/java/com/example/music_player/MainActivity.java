package com.example.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import android.Manifest;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {
    private final int READ_STORAGE_PERMISSION_REQUEST = 1;
    Intent intent;
    ListView songListView;
    ImageView searchButton;
    File[] songFolderFiles;
    ArrayList<String> nameOfSongs;
    ArrayList<Song> songDetails;

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

        // getSongs();
    }

    @AfterPermissionGranted(READ_STORAGE_PERMISSION_REQUEST)
    public void requestStoragePermissionForHigherVersions() {
        String perms = Manifest.permission.READ_MEDIA_AUDIO;
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

        nameOfSongs = new ArrayList<>();
        songDetails = new ArrayList<>();
    }

    public void clickables() {
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Entered Song");
                String songName = (String) parent.getItemAtPosition(position);
                System.out.println(position);

                intent = new Intent(MainActivity.this, SongActivity.class);
                intent.putExtra("songList", songDetails);
                intent.putExtra("position", position);
                startActivity(intent);
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
}