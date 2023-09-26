package com.example.music_player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RaagaActivity extends BottomSheetDialogFragment {
    private TextView raagaInfoText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_raaga, container, false);
        raagaInfoText = view.findViewById(R.id.raagaInfoText);

        // Fetch Raaga information from your API here
        // Display "Loading..." until data is fetched
        // When data is fetched, set it in raagaInfoText

        // Example of setting Raaga information (replace with actual data retrieval)
        String raagaInfo = "Loading...";
        raagaInfoText.setText(raagaInfo);

        return view;
    }
}

