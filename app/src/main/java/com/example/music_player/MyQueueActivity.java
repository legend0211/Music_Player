package com.example.music_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
public class MyQueueActivity extends Activity {
    ArrayList<Song> songDetails;
    ImageView backButton;
    ListView queueSongsListView;
    TextView songPresence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myqueue);
        initialisation();
        clickables();

        if(MainActivity.queueSongName.size()==0) {
            songPresence.setVisibility(View.VISIBLE);
            queueSongsListView.setVisibility(View.INVISIBLE);
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.queueSongName);
            queueSongsListView.setAdapter(adapter);
            queueSongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
        queueSongsListView = findViewById(R.id.queueSongsListView);
        songPresence = findViewById(R.id.songListPresence);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        queueSongsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showOptionsDialog(i);
                return true;
            }
        });
    }

    private void showOptionsDialog(int position) {
        final String[] options = {"Push to Top", "Remove"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    pushToTop(position);
                    break;
                case 1:
                    removeItem(position);
                    break;
            }
        });
        builder.create().show();
    }

    private void pushToTop(int position) {
        String selectedItem = MainActivity.queueSongName.get(position);
        MainActivity.queueSongName.remove(position);
        Song selectedSong = MainActivity.queueSongDetails.get(position);
        MainActivity.queueSongDetails.remove(position);
        MainActivity.queueSongName.add(0, selectedItem);
        MainActivity.queueSongDetails.add(0, selectedSong);
        Toast.makeText(getApplicationContext(), "Song Pushed to Top", Toast.LENGTH_SHORT).show();
        updateListView();
    }

    private void removeItem(int position) {
        MainActivity.queueSongName.remove(position);
        MainActivity.queueSongDetails.remove(position);
        Toast.makeText(getApplicationContext(), "Song Removed", Toast.LENGTH_SHORT).show();
        updateListView();
    }

    private void updateListView() {
        if (MainActivity.queueSongName.size() == 0) {
            songPresence.setVisibility(View.VISIBLE);
            queueSongsListView.setVisibility(View.INVISIBLE);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.queueSongName);
            queueSongsListView.setAdapter(adapter);
            queueSongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }
}
