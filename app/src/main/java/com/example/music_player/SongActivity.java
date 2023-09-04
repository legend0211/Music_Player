package com.example.music_player;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;

public class SongActivity extends Activity {
    ImageView imagePlayPause;
    TextView textCurrentTime, textTotalDuration;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    Handler handler;
    TextView textTitle, textArtist;
    int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        initialisation();
        clickables();

        String artistName = getIntent().getStringExtra("artistName");
        String songName = getIntent().getStringExtra("songName");
        String songPath = getIntent().getStringExtra("songPath");
        playSong(artistName, songName, songPath);
    }

    public void initialisation() {
        imagePlayPause = findViewById(R.id.buttonPlay);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalDuration = findViewById(R.id.textTotalTime);
        seekBar = findViewById(R.id.playerSeekBar);
        textTitle = findViewById(R.id.textTitle);
        textArtist = findViewById(R.id.textArtist);

        mediaPlayer = new MediaPlayer();
        handler = new Handler();
    }

    public void clickables() {
        imagePlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ImagePlayPause Clicked "+mediaPlayer.isPlaying());
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    duration = mediaPlayer.getCurrentPosition();
                    imagePlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(duration);
                    imagePlayPause.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
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
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            textTotalDuration.setText(millisecondsToMinutesAndSeconds(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            textCurrentTime.setText("0:00");
            updateSeekBar();
        } catch (IOException e) {
            e.printStackTrace();
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
            imagePlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacksAndMessages(null);
    }
}