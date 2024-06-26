package com.example.music_player;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class SongActivity extends FragmentActivity {
    static ImageView play_pauseButton, previousButton, nextButton, loopButton, favouritesButton, backButton, queueButton;
    static ImageView imageAlbumArt;
    static TextView textCurrentTime, textTotalDuration;
    static SeekBar seekBar;
    static MediaPlayer mediaPlayer;
    static Handler handler;
    static Handler raagaHandler;
    static FavouriteHelper favouriteHelper;
    static LibraryHelper libraryHelper;
    static TextView textTitle, textArtist, textSongDetails;
//    static TextView textRaagaName, textRaagaTime, textRaagaTherapy;
    static int duration, position, loopToggler;
    static ArrayList<Song> songDetails;
    static RaagaActivity bottomSheetFragment = new RaagaActivity();
    static Song currentSong;
    static int prev_counter = 1, downloaded = 0;
    static String textRaagaInfo = "", raagaName = "", raagaTime = "", raagaTherapy = "", raagaWeather = "";
    static String newPath;
    static Call<ResponseBody> call;
    static ProgressBar progressBar;
    static RelativeLayout progressBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        if(position==-2) {
            prev_counter = 0;
        }

        initialisation();
        clickables();
        songDetails = MainActivity.songDetails;

        if(prev_counter == 0) {
            position = -2;
            prev_counter = 1;
        }
        int flag = getIntent().getIntExtra("flag", 0);
        if(flag == 1) {
            songAlreadyPlaying();
        }
        else {
            position = getIntent().getIntExtra("position", -1);
            if (position == -1) {
                Toast.makeText(this, "No Song found", Toast.LENGTH_SHORT).show();
            }
            else if (position == -2) {
                Song uploadedSong = new Song();
                uploadedSong.name = getIntent().getStringExtra("name");
                uploadedSong.path = getIntent().getStringExtra("path");
                uploadedSong.thaat = "";
                playSong(getApplicationContext(), uploadedSong);
                favouritesButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                previousButton.setVisibility(View.GONE);
            }
            else {
                Song newSong = (Song) getIntent().getSerializableExtra("song");
                System.out.println("newSong: " + newSong.id);
                playSong(getApplicationContext(), newSong);
            }
        }
    }

    public void songAlreadyPlaying() {
        textTitle.setText(currentSong.name);
        textArtist.setText("");
        textTotalDuration.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getDuration()));
        seekBar.setMax(mediaPlayer.getDuration());

        if (!mediaPlayer.isPlaying()) {
            play_pauseButton.setImageResource(R.drawable.ic_play);
        } else {
            play_pauseButton.setImageResource(R.drawable.ic_pause);
        }
        updateSeekBar();
        if(!currentSong.favourites) {
            favouritesButton.setImageResource(R.drawable.ic_favorite_off);
        }
        else {
            favouritesButton.setImageResource(R.drawable.ic_favorite_on);
        }
        loopToggler = getIntent().getIntExtra("loop", 0);
        if(loopToggler == 0) {
            loopButton.setImageResource(R.drawable.ic_repeat_off);
        }
        else {
            loopButton.setImageResource(R.drawable.ic_repeat_on);
        }

        if(position>=0) {
            int imageResource = getApplicationContext().getResources().getIdentifier("song_img" + ((position % 12) + 1), "drawable", getApplicationContext().getPackageName());
            imageAlbumArt.setImageResource(imageResource);
        }
        else {
            int random = new Random().nextInt(12) + 1;
            int imageResource = getApplicationContext().getResources().getIdentifier("song_img" + ((random % 12) + 1), "drawable", getApplicationContext().getPackageName());
            imageAlbumArt.setImageResource(imageResource);
        }

    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
//        textRaagaName = findViewById(R.id.textRaagaName);
//        textRaagaTime = findViewById(R.id.textRaagaTime);
//        textRaagaTherapy = findViewById(R.id.textRaagaTherapy);
        textSongDetails = findViewById(R.id.textSongDetails);
        imageAlbumArt = findViewById(R.id.imageAlbumArt);

        play_pauseButton = findViewById(R.id.buttonPlay);
        previousButton = findViewById(R.id.buttonPrevious);
        nextButton = findViewById(R.id.buttonNext);
        loopButton = findViewById(R.id.buttonRepeat);
        favouritesButton = findViewById(R.id.buttonFavourites);
        queueButton = findViewById(R.id.queueMusic);

        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalDuration = findViewById(R.id.textTotalTime);
        seekBar = findViewById(R.id.playerSeekBar);

        textTitle = findViewById(R.id.textTitle);
        textArtist = findViewById(R.id.textArtist);

        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressBarLayout);

        position = -1;
        try {
            duration = mediaPlayer.getCurrentPosition();
        }
        catch (Exception e) {
            duration = 0;
        }
        loopToggler = 0;

        handler = new Handler();
        raagaHandler = new Handler();

        favouriteHelper = FavouriteHelper.getDB(this);
        libraryHelper = LibraryHelper.getDB(this);
    }

    public void clickables() {
        backButton.setOnClickListener(v -> finish());

        play_pauseButton.setOnClickListener(v -> {
//            System.out.println("play_pauseButton Clicked");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                duration = mediaPlayer.getCurrentPosition();
                play_pauseButton.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                play_pauseButton.setImageResource(R.drawable.ic_pause);
                updateSeekBar();
            }
        });

        nextButton.setOnClickListener(v -> {
            System.out.println("Next Button Clicked");
            progressBar.setVisibility(ProgressBar.VISIBLE);
            progressBarLayout.setVisibility(RelativeLayout.VISIBLE);

            prev_counter = 1;
            if(!MainActivity.queueSongName.isEmpty()) {
                currentSong = MainActivity.queueSongDetails.remove(0);
                MainActivity.queueSongName.remove(0);
                playSong(getApplicationContext(), currentSong);
            }
        });

        previousButton.setOnClickListener(v -> {
//                System.out.println("Previous Button Clicked");
            if(!MainActivity.librarySongName.isEmpty() && prev_counter < MainActivity.librarySongName.size()) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                progressBarLayout.setVisibility(RelativeLayout.VISIBLE);

                if(!MainActivity.queueSongDetails.get(0).path.equals(currentSong.path)) {
                    MainActivity.queueSongName.add(0, currentSong.name);
                    MainActivity.queueSongDetails.add(0, currentSong);
                }
                currentSong = MainActivity.librarySongDetails.get(prev_counter);

                ArrayList<LibrarySong> lib = (ArrayList<LibrarySong>) libraryHelper.librarySongDao().getAllLibrarySongs();
                for(int j=lib.size()-1; j>=0; j--) {
                    System.out.println(lib.get(j).getSongPath());
                    if(lib.get(j).getSongPath().equals(currentSong.path)) {
                        libraryHelper.librarySongDao().delete(lib.get(j));
                    }
                }
                MainActivity.librarySongDetails.remove(prev_counter);
                MainActivity.librarySongName.remove(prev_counter);
                prev_counter++;
                playSong(getApplicationContext(), currentSong);
            }
        });

        favouritesButton.setOnClickListener(v -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (currentSong.favourites) {
                        currentSong.favourites = false;
//                            favouriteHelper.favouriteSongDao().delete(new FavouriteSong(currentSong.id, currentSong.path));
                        favouriteHelper.favouriteSongDao().delete(new FavouriteSong(currentSong.path));
                        for (int i = 0; i < MainActivity.favouritesSongDetails.size(); i++) {
                            if (MainActivity.favouritesSongDetails.get(i).path.equals(currentSong.path)) {
                                MainActivity.favouritesSongDetails.remove(i);
                                MainActivity.favouritesSongName.remove(i);
                                break;
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                favouritesButton.setImageResource(R.drawable.ic_favorite_off);
                            }
                        });
                    }
                    else {
                        currentSong.favourites = true;
//                            favouriteHelper.favouriteSongDao().insert(new FavouriteSong(currentSong.id, currentSong.path));
                        favouriteHelper.favouriteSongDao().insert(new FavouriteSong(currentSong.path));
                        MainActivity.favouritesSongDetails.add(currentSong);
                        MainActivity.favouritesSongName.add(currentSong.name);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                favouritesButton.setImageResource(R.drawable.ic_favorite_on);
                            }
                        });
                    }
                }
            });
        });

        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.queueSongName.add(currentSong.name);
                MainActivity.queueSongDetails.add(currentSong);
                Toast.makeText(getApplicationContext(), "Song Added to Queue", Toast.LENGTH_SHORT).show();
            }
        });

        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.out.println("Loop Button Clicked");
                if(loopToggler == 1) {
                    loopToggler = 0;
                    loopButton.setImageResource(R.drawable.ic_repeat_off);
                }
                else {
                    loopToggler = 1;
                    loopButton.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });

        textSongDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textRaagaInfo = "Thaat: \n"+raagaName+"\n\n"+"Playing Time: \n"+raagaTime+"\n\n"+"Healing Therapies: \n"+raagaTherapy+"\n"+"Weather: \n"+raagaWeather;
                showRaagaInfoBottomSheet();
            }
        });

//        textRaagaName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RaagaActivity.ch = 1;
//                textRaagaInfo = raagaName;
//                showRaagaInfoBottomSheet();
//            }
//        });
//
//        textRaagaTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RaagaActivity.ch = 2;
//                textRaagaInfo = raagaTime;
//                showRaagaInfoBottomSheet();
//            }
//        });
//
//        textRaagaTherapy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RaagaActivity.ch = 3;
//                textRaagaInfo = raagaTherapy;
//                showRaagaInfoBottomSheet();
//            }
//        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    textCurrentTime.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getCurrentPosition()));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Method to show the Raaga info bottom sheet
    private void showRaagaInfoBottomSheet() {
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    public void progressBarUpdates() {
        try {
            MainActivity.progressBar.setVisibility(ProgressBar.GONE);
            MainActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            progressBar.setVisibility(ProgressBar.GONE);
            progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            SearchActivity.progressBar.setVisibility(ProgressBar.GONE);
            SearchActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            MyQueueActivity.progressBar.setVisibility(ProgressBar.GONE);
            MyQueueActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            LibraryActivity.progressBar.setVisibility(ProgressBar.GONE);
            LibraryActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            FavouritesActivity.progressBar.setVisibility(ProgressBar.GONE);
            FavouritesActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            WeatherActivity.progressBar.setVisibility(ProgressBar.GONE);
            WeatherActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}

        try {
            AllRagasActivity.progressBar.setVisibility(ProgressBar.GONE);
            AllRagasActivity.progressBarLayout.setVisibility(RelativeLayout.GONE);
        }
        catch (Exception e) {}
    }

    public void playSong(Context context, Song newSong) {
        progressBarUpdates();
        if(call!=null) {
            call.cancel();
        }
        RaagaActivity.ch = 0;
        textRaagaInfo = "";
        raagaName = "";
        raagaTherapy = "";
        raagaTime = "";
        raagaWeather = "";
        downloaded = 0;
        if(MainActivity.queueSongName.size()==0 && position!=-2) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    Set<Integer> selectedNumbers = new HashSet<>();
                    int list_len = 4;

                    while (selectedNumbers.size() < list_len) {
                        int randomNumber = random.nextInt(songDetails.size());
                        selectedNumbers.add(randomNumber);
                    }

                    for (int number : selectedNumbers) {
                        if(!songDetails.get(number).name.equals(newSong.name)) {
                            MainActivity.queueSongName.add(songDetails.get(number).name);
                            MainActivity.queueSongDetails.add(songDetails.get(number));
                        }
                    }
                }
            });
        }

        if(position>=0) {
            int imageResource = getApplicationContext().getResources().getIdentifier("song_img" + ((position % 12) + 1), "drawable", getApplicationContext().getPackageName());
            imageAlbumArt.setImageResource(imageResource);
        }
        else {
            int random = new Random().nextInt(12) + 1;
            int imageResource = getApplicationContext().getResources().getIdentifier("song_img" + ((random % 12) + 1), "drawable", getApplicationContext().getPackageName());
            imageAlbumArt.setImageResource(imageResource);
        }

        currentSong = newSong;

        textTitle.setText(newSong.name);
        textArtist.setText("");

        if(!currentSong.thaat.equals("")) {
            raagaName = currentSong.thaat;
            raagaTime = currentSong.time;
            raagaTherapy = getHealingTherapies(currentSong.thaat);
            raagaWeather = currentSong.weather;
        }
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(newSong.path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            play_pauseButton.setImageResource(R.drawable.ic_pause);
            textTotalDuration.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            textCurrentTime.setText("0:00");
            updateSeekBar();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(loopToggler==1) {
                        playNextSong(context);
                    }
                    else if(position!=-2) {
                        playNextSong(context);
                    }
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorServicee = Executors.newSingleThreadExecutor();
        executorServicee.execute(new Runnable() {
            @Override
            public void run() {
                if(newSong.favourites == false) {
                    favouritesButton.setImageResource(R.drawable.ic_favorite_off);
                }
                else {
                    favouritesButton.setImageResource(R.drawable.ic_favorite_on);
                }
                for (int i = 0; i < MainActivity.librarySongDetails.size(); i++) {
                    if (MainActivity.librarySongDetails.get(i).path.equals(newSong.path)) {
                        ArrayList<LibrarySong> lib = (ArrayList<LibrarySong>) libraryHelper.librarySongDao().getAllLibrarySongs();
                        for(int j=lib.size()-1; j>=0; j--) {
                            System.out.println(lib.get(j).getSongPath());
                            if(lib.get(j).getSongPath().equals(currentSong.path)) {
                                System.out.println("Entered delete library song");
                                libraryHelper.librarySongDao().delete(lib.get(j));
                            }
                        }

                        MainActivity.librarySongDetails.remove(i);
                        MainActivity.librarySongName.remove(i);
                    }
                }
                System.out.println("newSong: "+currentSong.id);
                libraryHelper.librarySongDao().insert(new LibrarySong(currentSong.id, currentSong.path, System.currentTimeMillis()));
                MainActivity.librarySongDetails.add(0, newSong);
                MainActivity.librarySongName.add(0, newSong.name);
            }
        });

        ExecutorService executorService3 = Executors.newSingleThreadExecutor();
        executorService3.execute(new Runnable() {
            @Override
            public void run() {
                raagaHandler.removeCallbacksAndMessages(null);
                raagaHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(call!=null) {
                            call.cancel();
                        }
                        newPath = "";
                        if (position != -2 && MainActivity.ch == 1) {
                            // newPath = downloadFiles(context);
                            newPath = currentSong.path;
                            System.out.println("Song Thaat "+currentSong.thaat);
                            if(currentSong.thaat.equals("")) {
                                apiCallForLink();
                            }
                        }
                        if (newPath.equals("")) {
                            newPath = currentSong.path;
                            apiCall();
                        }
                        System.out.println("New Path : " + newPath);
                    }
                }, 500);
            }
        });
    }

    public void playNextSong(Context context) {
        if(loopToggler == 1) {
            playSong(context, currentSong);
        }
        else {
            MainActivity.queueSongName.remove(0);
            playSong(context, MainActivity.queueSongDetails.remove(0));
        }
    }

    public static String millisecondsToMinutesAndSeconds(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        int minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        textCurrentTime.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getCurrentPosition()));
        textTotalDuration.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getDuration()));
        if (mediaPlayer.isPlaying()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };
            handler.postDelayed(runnable, 100);
        }
        else {
            play_pauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    public static void apiCall() {
        File audioFile = new File(newPath);
        ApiService apiService = ApiService.retrofit.create(ApiService.class);
        System.out.println("Size = " + audioFile.length());
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), audioFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("audio_file", audioFile.getName(), requestFile);
        call = apiService.uploadAudioFile(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                System.out.println("Response Code: " + response.code());
                System.out.println("Response Code: " + response.code());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray thaats = jsonObject.getJSONArray("thaat");
                    JSONArray times = jsonObject.getJSONArray("time");
                    JSONArray therapies = jsonObject.getJSONArray("therapy");

                    System.out.println("Thaat: " + thaats.getString(0));
                    for (int i = 0; i < thaats.length(); i++) {
                        raagaName += (i + 1) + ". " + thaats.getString(i) + "\n";
                    }
                    System.out.println("Time: " + times.getString(0));
                    for (int i = 0; i < times.length(); i++) {
                        raagaTime += (i + 1) + ". " + times.getString(i) + "\n";
                    }
                    System.out.println("Therapies:");
                    for (int i = 0; i < therapies.length(); i++) {
                        raagaTherapy += (i + 1) + ". " + therapies.getString(i) + "\n";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    audioFile.delete();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public static void apiCallForLink() {
        if (call != null && !call.isExecuted() && !call.isCanceled()) {
            call.cancel();
        }
        ApiService apiService = ApiService.retrofit.create(ApiService.class);
        Log.d(TAG, "" + newPath);
        call = apiService.uploadLink(newPath);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                System.out.println("Response Code: " + response.code());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray thaats = jsonObject.getJSONArray("thaat");
                    JSONArray times = jsonObject.getJSONArray("time");
                    JSONArray therapies = jsonObject.getJSONArray("therapy");

                    System.out.println("Thaat: " + thaats.getString(0));
                    for (int i = 0; i < thaats.length(); i++) {
                        raagaName += (i + 1) + ". " + thaats.getString(i) + "\n";
                    }
                    System.out.println("Time: " + times.getString(0));
                    for (int i = 0; i < times.length(); i++) {
                        raagaTime += (i + 1) + ". " + times.getString(i) + "\n";
                    }
                    System.out.println("Therapies:");
                    for (int i = 0; i < therapies.length(); i++) {
                        raagaTherapy += (i + 1) + ". " + therapies.getString(i) + "\n";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public static String getHealingTherapies(String str) {

        HashMap<String, String[]> ragas = new HashMap<>();
        ragas.put("Bilawal", new String[]{"Gives healthy mind and body", "Control sound and sonorous sleep"});
        ragas.put("Kalyan", new String[]{"Reduce mental tension", "Relief from headache",
                "Relief from cough and cold", "Relief from problems of high blood pressure",
                "Can cure Rheumatic Arthritis"});
        ragas.put("Khamaj", new String[]{"Control sound and sonorous sleep", "Reduce mental tension",
                "Relief from asthma", "Prevents hysteria"});
        ragas.put("Kafi", new String[]{"Gives healthy mind and body", "Brings joy", "Mitigate insomina",
                "Reduces anxiety", "Reduces hypertension"});
        ragas.put("Asavari", new String[]{"Reduce mental tension", "Reduces hypertension",
                "Brings Creativity and Happiness", "Cure low blood pressure",
                "Control psychological hazards and builds confidence", "Used for constipation"});
        ragas.put("Todi", new String[]{"Reduce mental tension", "Relief from headache", "Relief from cough and cold",
                "Prevents hysteria", "Reduces anxiety", "Brings serenity"});
        ragas.put("Poorvi", new String[]{"Reduce mental tension", "Mitigate insomina"});
        ragas.put("Marva", new String[]{"Gives healthy mind and body", "Prevents hysteria",
                "Enhances compassion and patience"});
        ragas.put("Bhairavi", new String[]{"Relief from headache", "Brings Creativity and Happiness",
                "Enhances compassion and patience", "Relief from High fever",
                "Relief from phlegm, toothache, intestinal gas, sinusitis"});
        ragas.put("Bhairav", new String[]{"Relief from headache", "Relief from cough and cold", "Brings serenity",
                "It can be used for Emotional strength, Devotion and Peace, Restful Sleep, Tranquility, Relaxation & Rest"});
        ragas.put("Mixed Thaat", new String[]{"Not Applicable"});

        String[] properties = ragas.get(str);
        String output = "";
        for (int i = 0; i < properties.length; i++) {
            output += (i+1) + ". " + properties[i] + "\n";
        }
        return output;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}