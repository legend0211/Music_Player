package com.example.music_player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

public class SongActivity extends Activity {
    ImageView play_pauseButton, previousButton, nextButton, loopButton, favouritesButton, backButton;
    TextView textCurrentTime, textTotalDuration;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    Handler handler;
    TextView textTitle, textArtist;
    int duration, position, favouriteToggler, loopToggler;
    ArrayList<Song> songDetails;


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
            String artistName = songDetails.get(position).artist;
            String songName = songDetails.get(position).name;
            String songPath = songDetails.get(position).path;
            playSong(artistName, songName, songPath);
        }
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);

        play_pauseButton = findViewById(R.id.buttonPlay);
        previousButton = findViewById(R.id.buttonPrevious);
        nextButton = findViewById(R.id.buttonNext);
        loopButton = findViewById(R.id.buttonRepeat);
        favouritesButton = findViewById(R.id.buttonFavourites);

        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalDuration = findViewById(R.id.textTotalTime);
        seekBar = findViewById(R.id.playerSeekBar);

        textTitle = findViewById(R.id.textTitle);
        textArtist = findViewById(R.id.textArtist);

        position = -1;
        duration = 0;
        favouriteToggler = 0;
        loopToggler = 0;

        mediaPlayer = new MediaPlayer();
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
                position++;
                if(position == songDetails.size()) {
                    position = 0;
                }
                String artistName = songDetails.get(position).artist;
                String songName = songDetails.get(position).name;
                String songPath = songDetails.get(position).path;
                playSong(artistName, songName, songPath);
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Previous Button Clicked");
                position--;
                if(position == -1) {
                    position = songDetails.size()-1;
                }
                String artistName = songDetails.get(position).artist;
                String songName = songDetails.get(position).name;
                String songPath = songDetails.get(position).path;
                playSong(artistName, songName, songPath);
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void playSong(String artistName, String songName, String songPath) {
        textTitle.setText(songName);
        textArtist.setText(artistName);

        // Create and configure a MediaPlayer to play the selected song
        try {
            onDestroy();
            mediaPlayer = new MediaPlayer();
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
        if(loopToggler == 0) {
            position++;
        }
        if (position == songDetails.size()) {
            position = 0;
        }
        String artistName = songDetails.get(position).artist;
        String songName = songDetails.get(position).name;
        String songPath = songDetails.get(position).path;
        playSong(artistName, songName, songPath);
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
        mediaPlayer.release();
        handler.removeCallbacksAndMessages(null);
    }
}