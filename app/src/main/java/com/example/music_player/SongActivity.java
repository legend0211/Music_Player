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

public class SongActivity extends Activity {

    private ImageView imagePlayPause;
    private TextView textCurrentTime, textTotalDuration;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        imagePlayPause = findViewById(R.id.buttonPlay);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalDuration = findViewById(R.id.textTotalTime);
        seekBar = findViewById(R.id.playerSeekBar);
        seekBar.setMax(100);
        mediaPlayer = new MediaPlayer();
        handler = new Handler();

    }

}
