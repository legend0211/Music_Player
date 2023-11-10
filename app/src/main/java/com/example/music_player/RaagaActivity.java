package com.example.music_player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RaagaActivity extends BottomSheetDialogFragment {
    static TextView raagaInfoText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_raaga, container, false);
        raagaInfoText = view.findViewById(R.id.raagaInfoText);

        if(SongActivity.textRaagaInfo.equals("")) {
            raagaInfoText.setText("Loading...");
        }
        else {
            raagaInfoText.setText(SongActivity.textRaagaInfo);
        }

        return view;
    }
}

