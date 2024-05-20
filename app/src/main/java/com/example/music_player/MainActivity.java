package com.example.music_player;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    static Intent intent;
    static Handler handler;
    static ListView songListView;
    static ImageView addButton, searchButton, myQueueList, libraryButton, accountButton, allRagasButton, musicGen;
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
    static int ch = 1, wId = 0;
    static String weatherNow = "";
    static String timeNow;
    static ArrayList<String> nameOfTimeSongs;
    static ArrayList<Song> timeSongDetails;
    static LinearLayout timeSongContainer;
    static ImageView weatherImageView;
    static ProgressBar progressBar;
    static RelativeLayout progressBarLayout;

    HashSet<Integer> cloudy;
    HashSet<Integer> rainy;
    HashSet<Integer> stormy;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.miniPlayerContainer, new MiniPlayerActivity()).commit();

        try {

        }
        catch (Exception e) {}

        initialisations();
        clickables();

        System.out.println("get");
        getSongs();

        calenderTimes();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (SongActivity.currentSong == null) {
                    frameLayout.setVisibility(View.GONE);
                } else {
                    frameLayout.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    public void initialisations() {
        searchButton = findViewById(R.id.Search);
        addButton = findViewById(R.id.addButton);
        songListView = findViewById(R.id.songsListView);
        myQueueList = findViewById(R.id.myQueueList);
        libraryButton = findViewById(R.id.library);
        accountButton = findViewById(R.id.account);
        frameLayout = findViewById(R.id.miniPlayerContainer);
        timeSongContainer = findViewById(R.id.itemContainer);
        weatherImageView = findViewById(R.id.imageViewWeather);
        allRagasButton = findViewById(R.id.allRagas);
        musicGen = findViewById(R.id.musicGen);
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressBarLayout);

        nameOfSongs = new ArrayList<>();
        songDetails = new ArrayList<>();
        nameOfTimeSongs = new ArrayList<>();
        timeSongDetails = new ArrayList<>();
        timeNow = "";

        cloudy = new HashSet<Integer>();
        cloudy.addAll(Arrays.asList(701, 741, 801, 802, 803, 804));

        rainy = new HashSet<Integer>();
        rainy.addAll(Arrays.asList(300, 301, 302, 310, 311, 312, 313, 314, 321, 500, 501, 502, 503, 504, 511, 520, 521, 522, 531, 600, 601, 602, 611, 612, 613, 615, 616, 620));

        stormy = new HashSet<Integer>();
        stormy.addAll(Arrays.asList(200, 201, 202, 210, 211, 212, 221, 230, 231, 232, 621, 622, 762, 771, 781));

        handler = new Handler();

        LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                fetchWeather(latitude, longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "No permission", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 0, locationListener);
    }

    private void fetchWeather(double latitude, double longitude) {
        String apiKey = "ac555aea90f9b3b0c87d3b2b3bc18a71";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JSONArray weatherArray = new JSONObject(response.toString()).getJSONArray("weather");
                        for (int i = 0; i < weatherArray.length(); i++) {
                            JSONObject weather = weatherArray.getJSONObject(i);
                            wId = weather.getInt("id");
                            // System.out.println("Weather ID: " + weatherId);
                        }
                        final int weatherId = wId;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(cloudy.contains(weatherId)) {
                                    weatherNow = "Cloudy";
                                    weatherImageView.setImageResource(R.drawable.img_cloudy);
                                }
                                else if(rainy.contains(weatherId)) {
                                    weatherNow = "Rainy";
                                    weatherImageView.setImageResource(R.drawable.img_rainy);
                                }
                                else if(stormy.contains(weatherId)) {
                                    weatherNow = "Stormy";
                                    weatherImageView.setImageResource(R.drawable.img_stormy);
                                }
                                else {
                                    weatherNow = "Sunny";
                                    Calendar calendar = Calendar.getInstance();
                                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                    if(hour>=18 || hour<=4) {
                                        weatherImageView.setImageResource(R.drawable.img_clear);
                                    }
                                    else {
                                        weatherImageView.setImageResource(R.drawable.img_sunny);
                                    }
                                }
                            }
                        });
                    } else {
                        System.out.println("Error: " + responseCode);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void clickables() {
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                progressBarLayout.setVisibility(RelativeLayout.VISIBLE);
//                ExecutorService songOpenExecutor = Executors.newSingleThreadExecutor();
//                songOpenExecutor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        intent = new Intent(MainActivity.this, SongActivity.class);
//                        intent.putExtra("song", songDetails.get(position));
//                        intent.putExtra("position", position);
//                        startActivity(intent);
//                    }
//                });
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

        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        weatherImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        allRagasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, AllRagasActivity.class);
                startActivity(intent);
            }
        });

        musicGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, SongGenerationActivity.class);
                startActivity(intent);
            }
        });
    }

    public void calenderTimes() {
        Handler calenderHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String currentTime = "";
                Calendar calendar = Calendar.getInstance();
                System.out.println("Date:" + calendar.getTime());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);

                if (hour >= 4 && hour < 6) {
                    currentTime = "Dawn";
                } else if (hour >= 6 && hour < 12) {
                    currentTime = "Morning";
                } else if (hour >= 12 && hour < 17) {
                    currentTime = "Afternoon";
                } else if (hour >= 17 && hour < 20) {
                    currentTime = "Evening";
                } else {
                    currentTime = "Night";
                }

                if(!currentTime.equals(timeNow)) {
                    timeNow = currentTime;
                    getTimeSongs();
                }
            }
        };
        calenderHandler.postDelayed(runnable, 100);
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

                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    progressBarLayout.setVisibility(RelativeLayout.VISIBLE);
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
        System.out.println("Database");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference songsCollectionRef = db.collection("songs");
        songsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        Map<String, Object> songData = document.getData();
                        Song song = new Song();
                        song.name = (String) songData.get("songName");
                        song.name = song.name.substring(0, song.name.lastIndexOf("."));
                        song.path = songData.get("downloadLink").toString();
                        song.time = songData.get("playingTime").toString();
                        song.thaat = songData.get("thaatName").toString();
                        song.weather = songData.get("weatherType").toString();

                        songDetails.add(song);
                        nameOfSongs.add(song.name);
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

    public void getTimeSongs() {
        System.out.println("Database Time");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference songsCollectionRef = db.collection("thaat");
        songsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    timeSongDetails.clear();
                    nameOfTimeSongs.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        Map<String, Object> songData = document.getData();
                        Song song = new Song();
                        song.name = (String) songData.get("songName");
                        song.path = songData.get("downloadLink").toString();
                        song.time = songData.get("playingTime").toString();
                        song.thaat = songData.get("thaatName").toString();
                        song.weather = songData.get("weatherType").toString();
                        timeSongDetails.add(song);
                        nameOfTimeSongs.add(song.name);
                    }
                    populateLinearLayout();
                } else {
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void populateLinearLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        timeSongContainer.removeAllViews();

        for (int i=0; i<timeSongDetails.size(); i++) {
            if(timeSongDetails.get(i).time.equals(timeNow)) {
                String ragaName = nameOfTimeSongs.get(i);
                LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.list_time_ragas, timeSongContainer, false);
                TextView textView = linearLayout.findViewById(R.id.textView);
                textView.setText(ragaName);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = nameOfTimeSongs.indexOf(ragaName);
                        if (index != -1 && index < timeSongDetails.size()) {
                            progressBar.setVisibility(ProgressBar.VISIBLE);
                            progressBarLayout.setVisibility(RelativeLayout.VISIBLE);
                            intent = new Intent(MainActivity.this, SongActivity.class);
                            intent.putExtra("song", timeSongDetails.get(index));
                            intent.putExtra("position", 0);
                            startActivity(intent);
                        }
                    }
                });
                timeSongContainer.addView(linearLayout);
            }
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
                    for(int i=0; i<timeSongDetails.size(); i++) {
                        if(timeSongDetails.get(i).path.equals(fav.get(j).getSongPath())) {
                            timeSongDetails.get(i).favourites = true;
                            Song song = timeSongDetails.get(i);
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
                    for(int i=0; i<songDetails.size(); i++) {
                        if(songDetails.get(i).path.equals(lib.get(j).getSongPath())) {
                            Song song = songDetails.get(i);
                            MainActivity.librarySongDetails.add(song);
                            MainActivity.librarySongName.add(song.name);
                            System.out.println(song.name);
                        }
                    }
                    for(int i=0; i<timeSongDetails.size(); i++) {
                        if(timeSongDetails.get(i).path.equals(lib.get(j).getSongPath())) {
                            Song song = timeSongDetails.get(i);
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
        builder.setTitle("Choose an action for adding to queue");

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