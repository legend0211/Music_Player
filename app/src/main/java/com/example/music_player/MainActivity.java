package com.example.music_player;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {
    private final int READ_STORAGE_PERMISSION_REQUEST = 1;
    private final int WRITE_STORAGE_PERMISSION_REQUEST = 1;
    static Intent intent;
    static Handler handler;
    static TextView textTitle;
    static TextView textSong;
    static ConstraintLayout miniPlayerLayout;
    static ListView songListView;
    static ImageView searchButton, myQueueList, favButton, libraryButton, accountButton, play_pauseButton;
    static Button addButton;
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
    File file;
    static int ch = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialisations();
        clickables();

        System.out.println("get");
        getSongs();
//        int currentApiVersion = Build.VERSION.SDK_INT;
//        requestWriteStoragePermission();
//        if(currentApiVersion>=33) {
//            requestStoragePermissionForHigherVersions();
//        }
//        else {
//            requestStoragePermission();
//        }

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

    @AfterPermissionGranted(WRITE_STORAGE_PERMISSION_REQUEST)
    public void requestWriteStoragePermission() {
        String perms = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (EasyPermissions.hasPermissions(this, perms)) {
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the write storage permission", WRITE_STORAGE_PERMISSION_REQUEST, perms);
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
        addButton = findViewById(R.id.addButtton);
        songListView = findViewById(R.id.songsListView);
        myQueueList = findViewById(R.id.myQueueList);
        favButton = findViewById(R.id.favourites);
        libraryButton = findViewById(R.id.library);
        accountButton = findViewById(R.id.account);
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
                intent = new Intent(MainActivity.this, SongActivity.class);
                intent.putExtra("song", songDetails.get(position));
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
                startActivity(intent);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload,1);
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

        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, AccountActivity.class);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();

            if (fileUri != null) {
                ContentResolver contentResolver = getContentResolver();
                try {
                    InputStream inputStream = contentResolver.openInputStream(fileUri);
                    String fileName = getFileNameFromUri(fileUri);

                    File downloadDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }
                    File downloadFile = new File(downloadDir, fileName);
                    OutputStream outputStream = new FileOutputStream(downloadFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    intent = new Intent(MainActivity.this, SongActivity.class);
                    intent.putExtra("path", downloadFile.getPath());
                    intent.putExtra("name", downloadFile.getName());
                    intent.putExtra("position", -2);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Can't recognize file. Please select again!", Toast.LENGTH_SHORT).show();
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload,1);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        String fileName = null;
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }
        return fileName;
    }

    public void getSongs() {
        if(ch == 0) {
            File songFolder = new File(Environment.getExternalStorageDirectory(), "SongFolder");
            songFolderFiles = songFolder.listFiles();
            System.out.println("Size = " + songFolderFiles.length);
            int c = 1;
            if (songFolderFiles != null) {
                for (File file : songFolderFiles) {
                    nameOfSongs.add(file.getName().substring(0, file.getName().length() - 4));
                    Song song = new Song();
                    song.id = c++;
                    song.artist = "";
                    song.name = nameOfSongs.get(nameOfSongs.size() - 1);
                    song.path = file.getPath();
                    songDetails.add(song);
                }
            }
            ListViewAdapter adapter = new ListViewAdapter(getApplicationContext(), nameOfSongs);
            songListView.setAdapter(adapter);
            fav_lib_db();
        }
        else {
            System.out.println("Database");
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference songsCollectionRef = db.collection("Songs");
            songsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            Map<String, Object> songData = document.getData();
                            Song song = new Song();
                            song.artist = (String) songData.get("artistName");
                            song.name = (String) songData.get("songName");
                            song.path = songData.get("download_link").toString();

                            songDetails.add(song);
                            nameOfSongs.add((String) songData.get("songName"));
                            System.out.println(song.path);

                            ListViewAdapter adapter = new ListViewAdapter(getApplicationContext(), nameOfSongs);
                            songListView.setAdapter(adapter);
                        }
                        fav_lib_db();
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void fav_lib_db() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FavouriteHelper favouriteHelper = FavouriteHelper.getDB(getApplicationContext());
                ArrayList<FavouriteSong> fav = (ArrayList<FavouriteSong>) favouriteHelper.favouriteSongDao().getAllFavoriteSongs();
                System.out.println("Fav db : "+fav);
                for(int j=0; j<fav.size(); j++) {
                    for(int i=0; i<songDetails.size(); i++) {
                        if(songDetails.get(i).path.equals(fav.get(j).getSongPath())) {
                            songDetails.get(i).favourites = true;
                            Song song = songDetails.get(i);
                            MainActivity.favouritesSongDetails.add(song);
                            MainActivity.favouritesSongName.add(song.name);
                            System.out.println(song.name);
                        }
                    }
                }

                LibraryHelper libraryHelper = LibraryHelper.getDB(getApplicationContext());
                ArrayList<LibrarySong> lib = (ArrayList<LibrarySong>) libraryHelper.librarySongDao().getAllLibrarySongs();
                System.out.println("Lib db : "+lib);
                for(int j=lib.size()-1; j>=0; j--) {
                    System.out.println(lib.get(j).getSongPath());
                    for(int i=0; i<songDetails.size(); i++) {
                        System.out.println(songDetails.get(i).path);
                        if(songDetails.get(i).path.equals(lib.get(j).getSongPath())) {
                            Song song = songDetails.get(i);
                            MainActivity.librarySongDetails.add(song);
                            MainActivity.librarySongName.add(song.name);
                            System.out.println(song.name);
                        }
                    }
                }
                System.out.println(librarySongName);
                System.out.println(favouritesSongName);
            }
        });
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