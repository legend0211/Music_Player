package com.example.music_player;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;

import androidx.fragment.app.FragmentActivity;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SongActivity extends FragmentActivity {
    static ImageView play_pauseButton, previousButton, nextButton, loopButton, favouritesButton, backButton, upButton, queueButton;
    static TextView textCurrentTime, textTotalDuration;
    static SeekBar seekBar;
    static MediaPlayer mediaPlayer;
    static Handler handler;
    static TextView textTitle, textArtist, textRaaga;
    static int duration, position, favouriteToggler, loopToggler;
    static ArrayList<Song> songDetails;
    Song currentSong = new Song();
    static boolean isRaagaVisible = false;
    static RaagaActivity bottomSheetFragment = new RaagaActivity();
    public static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");
    private static final String API_URL = "http://127.0.0.1:5000";

    static int prev_counter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        initialisation();
        clickables();

        songDetails = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        position = getIntent().getIntExtra("position", -1);
        if(position == -1) {
            Toast.makeText(this,"No Song found", Toast.LENGTH_SHORT).show();
        }
        else {
            currentSong.artist = songDetails.get(position).artist;
            currentSong.name = songDetails.get(position).name;
            currentSong.path = songDetails.get(position).path;
            playSong(currentSong.artist, currentSong.name, currentSong.path);
        }
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
        upButton = findViewById(R.id.imageUp);
        textRaaga = findViewById(R.id.textRaaga);

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

        position = -1;
        duration = 0;
        favouriteToggler = 0;
        loopToggler = 0;

        handler = new Handler();
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        play_pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("play_pauseButton Clicked");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    duration = mediaPlayer.getCurrentPosition();
                    play_pauseButton.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(duration);
                    play_pauseButton.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Next Button Clicked");
                prev_counter = 1;
                if(MainActivity.queueSongName.size()!=0) {
                    currentSong.artist = MainActivity.queueSongDetails.get(0).artist;
                    currentSong.name = MainActivity.queueSongName.get(0);
                    currentSong.path= MainActivity.queueSongDetails.get(0).path;

                    MainActivity.queueSongName.remove(0);
                    MainActivity.queueSongDetails.remove(0);
                    playSong(currentSong.artist, currentSong.name, currentSong.path);
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Previous Button Clicked");
                if(MainActivity.librarySongName.size()!=0 && prev_counter < MainActivity.librarySongName.size()) {
                    if(!MainActivity.queueSongDetails.get(0).path.equals(currentSong.path)) {
                        MainActivity.queueSongName.add(0, currentSong.name);
                        Song newSong = new Song();
                        newSong.path = currentSong.path;
                        newSong.name = currentSong.name;
                        newSong.artist = currentSong.artist;
                        MainActivity.queueSongDetails.add(0, newSong);
                    }
                    currentSong.artist = MainActivity.librarySongDetails.get(prev_counter).artist;
                    currentSong.name = MainActivity.librarySongName.get(prev_counter);
                    currentSong.path = MainActivity.librarySongDetails.get(prev_counter).path;

                    MainActivity.librarySongDetails.remove(prev_counter);
                    MainActivity.librarySongName.remove(prev_counter);
                    prev_counter++;
                    playSong(currentSong.artist, currentSong.name, currentSong.path);
                }
            }
        });

        favouritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Favourites Button Clicked");
                if(favouriteToggler == 1) {
                    favouriteToggler = 0;
                    favouritesButton.setImageResource(R.drawable.ic_favorite_off);
                }
                else {
                    favouriteToggler = 1;
                    favouritesButton.setImageResource(R.drawable.ic_favorite_on);
                }
            }
        });

        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.queueSongName.add(currentSong.name);
                Song newSong = new Song();
                newSong.path = currentSong.path;
                newSong.name = currentSong.name;
                newSong.artist = currentSong.artist;
                MainActivity.queueSongDetails.add(newSong);
                Toast.makeText(getApplicationContext(), "Song Added to Queue", Toast.LENGTH_SHORT).show();
            }
        });

        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Loop Button Clicked");
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

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRaagaInfoBottomSheet();
            }
        });

        textRaaga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRaagaInfoBottomSheet();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
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

    public void playSong(String artistName, String songName, String songPath) {
        ExecutorService executorServicee = Executors.newSingleThreadExecutor();
        executorServicee.execute(new Runnable() {
            @Override
            public void run() {
                currentSong.artist = artistName;
                currentSong.name = songName;
                currentSong.path = songPath;
                for (int i = 0; i < MainActivity.librarySongDetails.size(); i++) {
                    System.out.println(MainActivity.librarySongDetails.get(i).path+"..."+songPath);
                    if (MainActivity.librarySongDetails.get(i).path.equals(songPath)) {
                        MainActivity.librarySongDetails.remove(i);
                        MainActivity.librarySongName.remove(i);
                        break;
                    }
                }
                Song newSong = new Song();
                newSong.path = songPath;
                newSong.name = songName;
                newSong.artist = artistName;
                MainActivity.librarySongDetails.add(0, newSong);
                MainActivity.librarySongName.add(0, new String(currentSong.name));

                if (MainActivity.librarySongName.size() > 50) {
                    MainActivity.librarySongName.remove(50);
                    MainActivity.librarySongDetails.remove(50);
                }

                System.out.println("Current Song = " + currentSong.name);
                System.out.println("Prev Song List = " + MainActivity.librarySongName);
                System.out.println("Next Song List = " + MainActivity.queueSongName);
            }
        });

        if(MainActivity.queueSongName.size()==0) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Song> mixList = new ArrayList<>(songDetails);
                    long seed = System.nanoTime();
                    Collections.shuffle(mixList, new Random(seed));

                    int list_len = 6; //Actually 5
                    List<Song> selectedSongs = mixList.subList(0, list_len);
                    for (int i = 0; i < list_len; i++) {
                        if (selectedSongs.get(i).path.equals(songPath)) {
                            selectedSongs.remove(i);
                            break;
                        }
                    }
                    if (list_len == selectedSongs.size()) {
                        selectedSongs.remove(list_len - 1);
                    }
                    for (int i=0; i<selectedSongs.size(); i++) {
                        MainActivity.queueSongName.add(selectedSongs.get(i).name);
                        Song newSong = new Song();
                        newSong.path = selectedSongs.get(i).path;
                        newSong.name = selectedSongs.get(i).name;
                        newSong.artist = selectedSongs.get(i).artist;
                        MainActivity.queueSongDetails.add(newSong);
                    }
                }
            });
        }

        textTitle.setText(songName);
        textArtist.setText(artistName);

        // Create and configure a MediaPlayer to play the selected song
        try {
            if (mediaPlayer == null) {
                System.out.println("Mediaplayer is new");
                mediaPlayer = new MediaPlayer();
            } else {
                System.out.println("Mediaplayer is not new");
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            play_pauseButton.setImageResource(R.drawable.ic_pause);
            textTotalDuration.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            textCurrentTime.setText("0:00");

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNextSong();
                }
            });
            updateSeekBar();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playNextSong() {
        if(loopToggler == 1) {
            playSong(currentSong.artist, currentSong.name, currentSong.path);
        }
        else {
            currentSong.artist = MainActivity.queueSongDetails.get(0).artist;
            currentSong.name = MainActivity.queueSongName.get(0);
            currentSong.path = MainActivity.queueSongDetails.get(0).path;

            MainActivity.queueSongName.remove(0);
            MainActivity.queueSongDetails.remove(0);
            playSong(currentSong.artist, currentSong.name, currentSong.path);
        }
    }


    public String millisecondsToMinutesAndSeconds(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        int minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        textCurrentTime.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getCurrentPosition()));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}