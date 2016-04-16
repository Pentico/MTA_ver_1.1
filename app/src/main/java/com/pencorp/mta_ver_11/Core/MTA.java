package com.pencorp.mta_ver_11.Core;

/**
 * Created by Alfie on 2016/04/07.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.pencorp.mta_ver_11.Scanning_Songs.Song;
import com.pencorp.mta_ver_11.Scanning_Songs.SongList;
import com.pencorp.mta_ver_11.Core.MediaPlayerService.MusicBinder;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * The Kernel of this application Basically
 */
public class MTA {

    /**
     * ArrayList of Songs
     * This list will be edited alot during this applications lifetime
     *
     */
   public static ArrayList<Song> songs ;

    /**
     * All the songs on the device
     * Used By all the activities
     */
    public static SongList songList = new SongList();

    /**
     * Service that allows music to play
     */
    public static MediaPlayerService musicService = null;

    /**
     * List of songs currently being played
     */
    //TODO: will change this after debugging
    public static ArrayList<Song> nowPlayingList = songList.songs;

    /**
     * The actual connection to the MusicService.
     * We start it with an Intent.
     *
     * These callbacks will bind the MusicService to our internal
     * variables.
     * We can only know it happened through our flag, `musicBound`.
     */
    public static ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicBinder musicBinder = (MusicBinder)service;

            // Create the MusicService
            musicService = musicBinder.getService();
            musicService.setList(MTA.songs); //why ??
            musicService.musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            musicService.musicBound = false;
        }
    } ;

    /**
     * Our will to start a new music Service.
     * Android requires that we start a service through an Intent.
     */
    private static Intent musicServiceIntent = null;


    /**
     * Initializes the music Services at Activity/Context c.
     *
     * @note Only starts the service once - does nothing when called multiple times
     */
    public static void startMusicService(Context c){

        if (musicServiceIntent != null){
            return;
        }
        else if (MTA.musicService !=null){
            return;
        }
        else{

            //Create an intent to bind our music Connection to
            // the MusicService
            musicServiceIntent = new Intent(c, MediaPlayerService.class);
            c.bindService(musicServiceIntent,musicConnection,Context.BIND_AUTO_CREATE);
            c.startService(musicServiceIntent);
        }

    }




}
