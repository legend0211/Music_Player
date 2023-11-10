package com.example.music_player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchActivity extends Activity {
    ArrayList<Song> songDetails;
    EditText searchEditText;
    ImageView backButton;
    ListView searchSongsListView;
    TextView songPresence;
    ArrayList<String> filteredList = new ArrayList<>();
    ArrayList<Song> filteredListDetails = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initialisation();
        clickables();

        songDetails = MainActivity.songDetails;
        openKeyboard(searchEditText);
    }

    public void initialisation() {
        backButton = findViewById(R.id.backButtonSearchActivity);
        searchEditText = findViewById(R.id.searchBar);
        searchSongsListView = findViewById(R.id.searchSongsListView);
        songPresence = findViewById(R.id.songPresence);
    }

    public void clickables() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the keyboard is currently open
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    // Close the keyboard
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                finish();
            }
        });

        searchSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Entered Song");
                System.out.println(position);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    // Close the keyboard
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                Intent intent = new Intent(SearchActivity.this, SongActivity.class);
                intent.putExtra("song", filteredListDetails.get(position));
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                songPresence.setVisibility(View.INVISIBLE);
                searchSongsListView.setVisibility(View.INVISIBLE);

                if(!charSequence.toString().equals("")) {
                    String searchText = charSequence.toString().toLowerCase();

                    ArrayList<String> fltList = new ArrayList<String>();
                    ArrayList<Song> fltListDetails = new ArrayList<Song>();
                    for (Song song : songDetails) {
                        if (song.name.toLowerCase().contains(searchText)) {
                            fltList.add(song.name);
                            fltListDetails.add(song);
                        }
                    }
                    filteredListDetails = fltListDetails;
                    filteredList = fltList;
                    if (filteredList.size() == 0) {
                        songPresence.setVisibility(View.VISIBLE);
                    } else {
                        ListViewAdapter adapter = new ListViewAdapter(SearchActivity.this, fltList);
                        searchSongsListView.setAdapter(adapter);
                        searchSongsListView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used in this case
            }
        });
    }
    public void openKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}
