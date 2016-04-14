package com.pencorp.mta_ver_11.Debuging_Tol;

import android.media.session.MediaController;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pencorp.mta_ver_11.Activities.General.CoreActivity;
import com.pencorp.mta_ver_11.Activities.General.MainActivity;
import com.pencorp.mta_ver_11.Core.MTA;
import com.pencorp.mta_ver_11.R;

public class Play extends CoreActivity  implements
        android.widget.MediaController.MediaPlayerControl,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{



    /**
     * List that will display all the songs.
     */
    private ListView songListView;

    private boolean paused = false;
    private boolean playbackPaused = false;


    /**
     * Thing that maps songs to items on the ListView.
     *
     * We're keeping track of it so we can refresh the ListView if the user
     * wishes to change it's order.
     *
     * Check out the leftmost menu and it's options.
     */
    private AdapterSong songAdapter;

    //making the music play
    private MusicController musicController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        songListView = (ListView) findViewById(R.id.list_view);



        // We'll play this pre-defined list.
        // By default we play the first track, although an
        // extra can change this. Look below.
        MTA.musicService.setList(MTA.nowPlayingList);
        MTA.musicService.setSong(0);

        // Connects the song list to an adapter
        // (thing that creates several Layouts from the song list)
        songAdapter = new AdapterSong(this, MTA.nowPlayingList);
        songListView.setAdapter(songAdapter);

        MTA.musicService.playSong(); //Not sure if it will play without the bundle if statement..

        // Scroll the list view to the current song.
        songListView.setSelection(MTA.musicService.currentSongPosition);

        setMusicController();

        //TODO :Later ...........
        // While we're playing music, add an item to the
        // Main Menu that returns here.
        //MainActivity.addNowPlayingItem(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    /**
     * (Re)Starts the musicController.
     */
    private void setMusicController(){

        musicController = new MusicController(Play.this);

        //what will happen when the uer presses the next/previous buttons
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Calling method defined on  Play.c
                playNext();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Calling method defined on Play.c
                playPrevious();

            }
        });

        //Binding to our media player
        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.list_view));
        musicController.setEnabled(true);
    }

    /**
     * Jumps to the next song and starts playing it right now.
     */
    public void playNext() {
        MTA.musicService.next(true);
        MTA.musicService.playSong();

        // To prevent the MusicPlayer from behaving
        // unexpectedly when we pause the song playback.
        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }

        musicController.show();
    }

    /**
     * Jumps to the previous song and starts playing it right now.
     */
    public void playPrevious() {
        MTA.musicService.previous(true);
        MTA.musicService.playSong();


        // To prevent the MusicPlayer from behaving
        // unexpectedly when we pause the song playback.
        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }

        musicController.show();
    }

    @Override
    public void start() {
        MTA.musicService.unpausePlayer();
    }
    /**
     * Callback to when the user pressed the `pause` button.
     */
    @Override
    public void pause() {

        MTA.musicService.pausePlayer();
    }

    @Override
    public int getDuration() {

        if (MTA.musicService != null && MTA.musicService.musicBound
                && MTA.musicService.isPlaying()) {

            return MTA.musicService.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {

        if(MTA.musicService != null && MTA.musicService.musicBound
                && MTA.musicService.isPlaying()){


            return MTA.musicService.getPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {

        //TODO: Seek to implement
    }

    @Override
    public boolean isPlaying() {

        if(MTA.musicService != null && MTA.musicService.musicBound) {

            return MTA.musicService.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        // TODO Auto-generaated method stud
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
