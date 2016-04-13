package com.pencorp.mta_ver_11.Debuging_Tol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pencorp.mta_ver_11.R;
import com.pencorp.mta_ver_11.Scanning_Songs.Song;

import java.util.ArrayList;

/**
 * Created by Alfie on 2016/04/07.
 */
public class AdapterSong extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    public AdapterSong(Context c,ArrayList<Song> theSongs){
        songs = theSongs;
        songInflater = LayoutInflater.from(c);
    }


    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }


    @Override
    public long getItemId(int position) {
        //TODO auto generated method stud
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Map from a Song to a Song Layout
        LinearLayout songLayout = (LinearLayout)songInflater.inflate(R.layout.menu_item_song,parent,false);

        TextView titleView  = (TextView)songLayout.findViewById(R.id.menu_item_song_title);
        TextView artistView = (TextView)songLayout.findViewById(R.id.menu_item_song_artist);
        TextView albumView  = (TextView)songLayout.findViewById(R.id.menu_item_song_album);

        Song currentSong = songs.get(position);


        String title = currentSong.getTitle();
        if (title.isEmpty())
            titleView.setText("<unknown>");
        else
            titleView.setText(currentSong.getTitle());

        String artist = currentSong.getArtist();
        if (artist.isEmpty())
            artistView.setText("<unknown>");
        else
            artistView.setText(currentSong.getArtist());

        String album = currentSong.getAlbum();
        if (album.isEmpty())
            albumView.setText("<unknown>");
        else
            albumView.setText(currentSong.getAlbum());


        // Saving position as a tag.
        // Each Song layout has a onClick attribute,
        // which calls a function that plays a song
        // with that tag.
        songLayout.setTag(position);
        return songLayout ;
    }
}
