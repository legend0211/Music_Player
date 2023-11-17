package com.example.music_player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RaagaActivity extends BottomSheetDialogFragment {
    static TextView raagaInfoText;
    private Handler handler;
    static int ch = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_raaga, container, false);
        raagaInfoText = view.findViewById(R.id.raagaInfoText);

        handler = new Handler(Looper.getMainLooper());
        updateRaagaInfoText();

        return view;
    }

    public void updateRaagaInfoText() {
        if (SongActivity.textRaagaInfo.equals("")) {
            raagaInfoText.setText("Loading...");
        } else {
            raagaInfoText.setText(SongActivity.textRaagaInfo);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRaagaInfoText();
            }
        }, 200);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
