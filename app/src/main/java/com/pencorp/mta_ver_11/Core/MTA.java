package com.pencorp.mta_ver_11.Core;

/**
 * Created by Alfie on 2016/04/07.
 */

import com.pencorp.mta_ver_11.Scanning_Songs.Song;
import com.pencorp.mta_ver_11.Scanning_Songs.SongList;

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
   public static ArrayList<Song> songs = null;

    /**
     * All the songs on the device
     * Used By all the activities
     */
    public static SongList songList = new SongList();

}
