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
    static int duration, position, loopToggler;
    static ArrayList<Song> songDetails;
    static boolean isRaagaVisible = false;
    static RaagaActivity bottomSheetFragment = new RaagaActivity();
    public static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");
    private static final String API_URL = "http://127.0.0.1:5000";
    static Song currentSong;

    static int prev_counter = 1;


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
            else if(position == -2) {
                Song uploadedSong = new Song();
                uploadedSong.name = getIntent().getStringExtra("name");
                uploadedSong.artist = "NULL";
                uploadedSong.path = getIntent().getStringExtra("path");
                playSong(uploadedSong);
            }
            else {
                playSong(songDetails.get(position));
            }
        }
    }

    public void songAlreadyPlaying() {
        MainActivity.textTitle.setText(currentSong.name);
        textTitle.setText(currentSong.name);
        textArtist.setText(currentSong.artist);
        textTotalDuration.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getDuration()));
        seekBar.setMax(mediaPlayer.getDuration());

        if (!mediaPlayer.isPlaying()) {
            play_pauseButton.setImageResource(R.drawable.ic_play);
        } else {
            play_pauseButton.setImageResource(R.drawable.ic_pause);
        }
        updateSeekBar();
        if(currentSong.favourites == false) {
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
//                System.out.println("play_pauseButton Clicked");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play_pauseButton.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    System.out.println(mediaPlayer.getCurrentPosition());
                    play_pauseButton.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.out.println("Next Button Clicked");
                prev_counter = 1;
                if(MainActivity.queueSongName.size()!=0) {
//                    for(int i=0; i<MainActivity.queueSongDetails.size(); i++)
//                        System.out.print(MainActivity.queueSongDetails.get(i).name +" ");
                    currentSong = MainActivity.queueSongDetails.remove(0);
                    MainActivity.queueSongName.remove(0);
                    playSong(currentSong);
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.out.println("Previous Button Clicked");
                if(MainActivity.librarySongName.size()!=0 && prev_counter < MainActivity.librarySongName.size()) {
                    if(!MainActivity.queueSongDetails.get(0).path.equals(currentSong.path)) {
                        MainActivity.queueSongName.add(0, currentSong.name);
                        MainActivity.queueSongDetails.add(0, currentSong);
                    }
                    currentSong = MainActivity.librarySongDetails.get(prev_counter);
                    MainActivity.librarySongDetails.remove(prev_counter);
                    MainActivity.librarySongName.remove(prev_counter);
                    prev_counter++;
                    playSong(currentSong);
                }
            }
        });

        favouritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.out.println("Favourites Button Clicked");
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(currentSong.favourites == true) {
                            currentSong.favourites = false;
                            for(int i=0; i<MainActivity.favouritesSongDetails.size(); i++) {
                                if(MainActivity.favouritesSongDetails.get(i).path.equals(currentSong.path)) {
                                    MainActivity.favouritesSongDetails.remove(i);
                                    MainActivity.favouritesSongName.remove(i);
                                    break;
                                }
                            }
                            favouritesButton.setImageResource(R.drawable.ic_favorite_off);
                        }
                        else {
                            currentSong.favourites = true;
                            MainActivity.favouritesSongDetails.add(currentSong);
                            MainActivity.favouritesSongName.add(currentSong.name);
                            favouritesButton.setImageResource(R.drawable.ic_favorite_on);
                        }
                    }
                });
            }
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

    public static void playSong(Song newSong) {
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
//                    System.out.println(MainActivity.librarySongDetails.get(i).path+"..."+newSong.path);
                    if (MainActivity.librarySongDetails.get(i).path.equals(newSong.path)) {
                        MainActivity.librarySongDetails.remove(i);
                        MainActivity.librarySongName.remove(i);
                        break;
                    }
                }
                MainActivity.librarySongDetails.add(0, newSong);
                MainActivity.librarySongName.add(0, newSong.name);

                if (MainActivity.librarySongName.size() > 50) {
                    MainActivity.librarySongName.remove(50);
                    MainActivity.librarySongDetails.remove(50);
                }

//                System.out.println("Current Song = " + newSong.name);
//                System.out.println("Prev Song List = " + MainActivity.librarySongName);
//                System.out.println("Next Song List = " + MainActivity.queueSongName);
            }
        });

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

        currentSong = newSong;

        textTitle.setText(newSong.name);
        textArtist.setText(newSong.artist);

        // Create and configure a MediaPlayer to play the selected song
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
                        playNextSong();
                    }
                    else if(position!=-2) {
                        playNextSong();
                    }
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void playNextSong() {
        if(loopToggler == 1) {
            playSong(currentSong);
        }
        else {
            MainActivity.queueSongName.remove(0);
            playSong(MainActivity.queueSongDetails.remove(0));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}