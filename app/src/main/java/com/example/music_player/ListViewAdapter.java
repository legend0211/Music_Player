package com.example.music_player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> nameOfSongs;

    public ListViewAdapter(Context context, List<String> nameOfSongs) {
        this.context = context;
        this.nameOfSongs = nameOfSongs;
    }

    @Override
    public int getCount() {
        return nameOfSongs.size();
    }

    @Override
    public Object getItem(int position) {
        return nameOfSongs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_song2, parent, false);
        }

        String songName = nameOfSongs.get(position);

        TextView songNameTextView = convertView.findViewById(R.id.songNameTextView);
        songNameTextView.setText(songName);

        // You can set other views in your custom layout here as well.

        return convertView;
    }
}
