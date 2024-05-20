package com.example.music_player;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MiniPlayerActivity extends Fragment {
    private ImageView play_pauseButton, favouritesButton;
    private TextView textTitle;
    private ConstraintLayout miniPlayerLayout;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mini_player, container, false);
        initialisation(view);
        clickables();
        setupAutoRefresh();
        return view;
    }

    private void initialisation(View view) {
        textTitle = view.findViewById(R.id.miniTextTitle);
        favouritesButton = view.findViewById(R.id.miniFavouriteButton);
        play_pauseButton = view.findViewById(R.id.miniPlayButton);
        miniPlayerLayout = view.findViewById(R.id.miniPlayer);
    }

    public void clickables() {
        miniPlayerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), SongActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("flag", 1);
                intent.putExtra("loop", SongActivity.loopToggler);
                startActivity(intent);
            }
        });

        play_pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("play_pauseButton Clicked");
                if (SongActivity.mediaPlayer.isPlaying()) {
                    SongActivity.mediaPlayer.pause();
                    SongActivity.duration = SongActivity.mediaPlayer.getCurrentPosition();
                    play_pauseButton.setImageResource(R.drawable.ic_play);
                } else {
                    SongActivity.mediaPlayer.start();
                    SongActivity.mediaPlayer.seekTo(SongActivity.duration);
                    play_pauseButton.setImageResource(R.drawable.ic_pause);
                    SongActivity.updateSeekBar();
                }
            }
        });

        favouritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (SongActivity.currentSong.favourites) {
                            System.out.println("Fav set to off");
                            SongActivity.currentSong.favourites = false;
//                            SongActivity.favouriteHelper.favouriteSongDao().delete(new FavouriteSong(SongActivity.currentSong.id, SongActivity.currentSong.path));
                            SongActivity.favouriteHelper.favouriteSongDao().delete(new FavouriteSong(SongActivity.currentSong.path));
                            for (int i = 0; i < MainActivity.favouritesSongDetails.size(); i++) {
                                if (MainActivity.favouritesSongDetails.get(i).path.equals(SongActivity.currentSong.path)) {
                                    MainActivity.favouritesSongDetails.remove(i);
                                    MainActivity.favouritesSongName.remove(i);
                                    break;
                                }
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    favouritesButton.setImageResource(R.drawable.ic_favorite_off);
                                }
                            });
                        } else {
                            System.out.println("Fav set to on");
                            SongActivity.currentSong.favourites = true;
//                            SongActivity.favouriteHelper.favouriteSongDao().insert(new FavouriteSong(SongActivity.currentSong.id, SongActivity.currentSong.path));
                            SongActivity.favouriteHelper.favouriteSongDao().insert(new FavouriteSong(SongActivity.currentSong.path));
                            MainActivity.favouritesSongDetails.add(SongActivity.currentSong);
                            MainActivity.favouritesSongName.add(SongActivity.currentSong.name);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    favouritesButton.setImageResource(R.drawable.ic_favorite_on);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void setupAutoRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Update mini player UI based on current song or playback state
                updateMiniPlayerUI();
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    private void updateMiniPlayerUI() {
        // Update mini player UI elements based on current song or playback state
        if (SongActivity.currentSong == null) {
            miniPlayerLayout.setVisibility(View.GONE);
        } else {
            miniPlayerLayout.setVisibility(View.VISIBLE);
            textTitle.setText(SongActivity.currentSong.name);
            if (SongActivity.currentSong.favourites) {
                favouritesButton.setImageResource(R.drawable.ic_favorite_on);
            } else {
                favouritesButton.setImageResource(R.drawable.ic_favorite_off);
            }
        }
        try {
            if (SongActivity.mediaPlayer.isPlaying()) {
                play_pauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                play_pauseButton.setImageResource(R.drawable.ic_play);
            }
        }
        catch (Exception e) {}
    }
}
