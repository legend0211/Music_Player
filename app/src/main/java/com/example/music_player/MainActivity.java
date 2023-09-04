package com.example.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import android.Manifest;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;



public class MainActivity extends AppCompatActivity {
    private final int READ_STORAGE_PERMISSION_REQUEST = 1;
    Intent intent;
    ListView songListView;
    ImageView imgv;
    File[] songFolderFiles;
    ArrayList<String> nameOfSongs;
    HashMap<String, Song> map;

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

//        getSongs();
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
        imgv = findViewById(R.id.Search);
        songListView = findViewById(R.id.songsListView);

        nameOfSongs = new ArrayList<>();
        map = new HashMap<>();
    }

    public void clickables() {
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Entered Song");
                String songName = (String)parent.getItemAtPosition(position);
                if(map.containsKey(songName)) {
                    intent = new Intent(MainActivity.this, SongActivity.class);
                    intent.putExtra("artistName", map.get(songName).artist);
                    intent.putExtra("songName", map.get(songName).name);
                    intent.putExtra("songPath", map.get(songName).path);
                    startActivity(intent);
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
                nameOfSongs.add(file.getName().substring(0,file.getName().length()-4));
                Song song = new Song();
                song.artist = "Anonymous";
                song.name = nameOfSongs.get(nameOfSongs.size()-1);
                song.path = file.getPath();
                map.put(song.name, song);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameOfSongs);
        songListView.setAdapter(adapter);
    }
}