package com.example.music_player;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LibraryActivity extends Activity {
    ArrayList<Song> songDetails;
    ImageView backButton;
    ListView librarySongsListView;
    TextView songPresence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        initialisation();
        clickables();

        if(MainActivity.librarySongName.size()==0) {
            songPresence.setVisibility(View.VISIBLE);
            librarySongsListView.setVisibility(View.INVISIBLE);
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.librarySongName);
            librarySongsListView.setAdapter(adapter);
            librarySongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButton);
        librarySongsListView = findViewById(R.id.librarySongsListView);
        songPresence = findViewById(R.id.songListPresence);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        librarySongsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showOptionsDialog(i);
                return true;
            }
        });
    }

    private void showOptionsDialog(int position) {
        final String[] options = {"Remove"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    removeItem(position);
                    break;
            }
        });
        builder.create().show();
    }

    private void removeItem(int position) {
        MainActivity.librarySongDetails.remove(position);
        MainActivity.librarySongName.remove(position);
        Toast.makeText(getApplicationContext(), "Song Removed", Toast.LENGTH_SHORT).show();
        updateListView();
    }

    private void updateListView() {
        if (MainActivity.librarySongName.size() == 0) {
            songPresence.setVisibility(View.VISIBLE);
            librarySongsListView.setVisibility(View.INVISIBLE);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_song, MainActivity.librarySongName);
            librarySongsListView.setAdapter(adapter);
            librarySongsListView.setVisibility(View.VISIBLE);
            songPresence.setVisibility(View.INVISIBLE);
        }
    }
}
