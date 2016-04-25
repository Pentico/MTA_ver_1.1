package com.pencorp.mta_ver_11.Core;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pencorp.mta_ver_11.Scanning_Songs.Song;
import com.pencorp.mta_ver_11.Scanning_Songs.SongList;


/**
 * Created by Alfie on 2016/04/12.
 */
public class MediaPlayerService extends Service
        implements  MediaPlayer.OnPreparedListener,
                    MediaPlayer.OnErrorListener,
                    MediaPlayer.OnCompletionListener,
                    AudioManager.OnAudioFocusChangeListener {

    //////////////////////////////////////////Declarations/////////////////////////////////

    //Debugging
    static String TAG = "MediaPlayerService";


    /**
     * Android Media Player
     */
    private MediaPlayer mediaPlayer;

    /**
     * List of song we're currently playing
     */
    private static ArrayList<Song> songs = MTA.nowPlayingList;

    /**
     * Copy of the Current song being played (paused)
     *
     * Use it to get info from the current song.
     */
    public Song currentSong = null;

    public Song songToPlay;

    /**
     * Index of the current song we're playing on the songs list
     */
    public int currentSongPosition=0;

    /**
     * Possible states this Service can be on
     */
    enum  ServiceState {

        //MediaPlayer is stopped and not prepared to play
        Stopped,

        //MediaPlayer is preparing
        Preparing,

        //Playback active - media player ready!
        //(But the media player may actualy be paused in
        //this state if we don't have audio focus
        Playing,

        //So that we know we have to resume playback once we get focus back
        Paused
    }

    /**
     * Current state of the Service.
     */
    ServiceState serviceState = ServiceState.Preparing;

    /**
     * Use this to get audio focus:
     *
     * 1. Making sure other music apps don't play
     *    at the same time;
     * 2. Guaranteeing the lock screen widget will
     *    be controlled by us;
     */
    AudioManager audioManager;


    /**************** BROADCAST_SERVICE ***************************/

    /**
     * String that identifies all broadcasts this Service makes.
     *
     * Since this Service will send LocalBroadcasts to explain
     * what it does (like "playing song" or "paused song"),
     * other classes that might be interested on it must
     * register a BroadcastReceiver to this String.
     */
    public static final String BROADCAST_ACTION = "com.pencorp.mta_ver_11";

    /** String used to get the current state Extra on the Broadcast Intent */
    public static final String BROADCAST_EXTRA_STATE = "x_japan";

    /** String used to get the song ID Extra on the Broadcast Intent */
    public static final String BROADCAST_EXTRA_SONG_ID = "tenacious_d";

    // All possible messages this Service will broadcast
    // Ignore the actual values

    /** Broadcast for when some music started playing */
    public static final String BROADCAST_EXTRA_PLAYING = "beatles";

    /** Broadcast for when some music just got paused */
    public static final String BROADCAST_EXTRA_PAUSED = "santana";

    /** Broadcast for when a paused music got unpaused*/
    public static final String BROADCAST_EXTRA_UNPAUSED = "iron_maiden";

    /** Broadcast for when current music got played until the end */
    public static final String BROADCAST_EXTRA_COMPLETED = "los_hermanos";

    /** Broadcast for when the user skipped to the next song */
    public static final String BROADCAST_EXTRA_SKIP_NEXT = "paul_gilbert";

    /** Broadcast for when the user skipped to the previous song */
    public static final String BROADCAST_EXTRA_SKIP_PREVIOUS = "john_petrucci";


    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String BROADCAST_ORDER = "com.pencorp.mta_ver_11.MUSIC_SERVICE";
    public static final String BROADCAST_EXTRA_GET_ORDER = "com.pencorp.mta_ver_11.dasdas.MUSIC_SERVICE";

    public static final String BROADCAST_ORDER_PLAY            = "com.pencorp.mta_ver_11.action.PLAY";
    public static final String BROADCAST_ORDER_PAUSE           = "com.pencorp.mta_ver_11.action.PAUSE";
    public static final String BROADCAST_ORDER_TOGGLE_PLAYBACK = "dlsadasd";
    public static final String BROADCAST_ORDER_STOP            = "com.pencorp.mta_ver_11.action.STOP";
    public static final String BROADCAST_ORDER_SKIP            = "com.pencorp.mta_ver_11.action.SKIP";
    public static final String BROADCAST_ORDER_REWIND          = "com.pencorp.mta_ver_11.action.REWIND";





    /******************* END **************************************/



    /**
     * Whenever we're created, reset the MusicPlayer and
     * start the MusicScrobblerService.
     */
    public void onCreate() {
        super.onCreate();

        songs = MTA.nowPlayingList;
        //reset the songs list
        currentSongPosition = 0;

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        initMusicPlayer();

        Log.w(TAG, "onCreate");
    }

    /**
     * Initializes the Android's internal MediaPlayer.
     *
     * @note We might call this function several times without
     *       necessarily calling {@link #stopMusicPlayer()}.
     */
    public void initMusicPlayer(){

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }

        // Assures the CPU continues running this service
        // even when the device is sleeping.
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // These are the events that will "wake us up"
        mediaPlayer.setOnPreparedListener(this); // player initialized
        mediaPlayer.setOnCompletionListener(this); // song completed
        mediaPlayer.setOnErrorListener(this);

        Log.w(TAG, "initMusicPlayer");
    }

    /**
     * Cleans resources from Android's native MediaPlayer.
     *
     * @note According to the MediaPlayer guide, you should release
     *       the MediaPlayer as often as possible.
     *       For example, when losing Audio Focus for an extended
     *       period of time.
     */
    public void stopMusicPlayer() {
        if (mediaPlayer == null)
            return;

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        Log.w(TAG, "stopMusicPlayer");
    }

    /**
     * Sets the "Now Playing List"
     *
     * @param theSongs Songs list that will play from now on.
     *
     * @note Make sure to call {@link #playSong()} after this.
     */
    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    /**
     * The thing that will keep an eye on LocalBroadcasts
     * for the MusicService.
     */
    BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Getting the information sent by the MusicService
            // (and ignoring it if invalid)
            String order = intent.getStringExtra(MediaPlayerService.BROADCAST_EXTRA_GET_ORDER);

            // What?
            if (order == null)
                return;

            if (order.equals(MediaPlayerService.BROADCAST_ORDER_PAUSE)) {
                pausePlayer();
            }
            else if (order.equals(MediaPlayerService.BROADCAST_ORDER_PLAY)) {
                unpausePlayer();
            }
            else if (order.equals(MediaPlayerService.BROADCAST_ORDER_SKIP)) {
                next(true);
                playSong();
            }
            else if (order.equals(MediaPlayerService.BROADCAST_ORDER_REWIND)) {
                previous(true);
                playSong();
            }

            Log.w(TAG, "local broadcast received");
        }
    };

    /**
     * Asks the AudioManager for our application to
     * have the audio focus.
     *
     * @return If we have it.
     */
    private boolean requestAudioFocus() {
        //Request audio focus for playback
        int result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        //Check if audio focus was granted. If not, stop the service.
        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    /**
     * Does something when the audio focus state changed
     *
     * @note Meaning it runs when we get and when we don't get
     *       the audio focus from `#requestAudioFocus()`.
     *
     * For example, when we receive a message, we lose the focus
     * and when the ringer stops playing, we get the focus again.
     *
     * So we must avoid the bug that occurs when the user pauses
     * the player but receives a message - and since after that
     * we get the focus, the player will unpause.
     */
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {

            // Yay, gained audio focus! Either from losing it for
            // a long or short periods of time.
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.w(TAG, "audiofocus gain");

                if (mediaPlayer == null)
                    initMusicPlayer();

                if (pausedTemporarilyDueToAudioFocus) {
                    pausedTemporarilyDueToAudioFocus = false;
                    unpausePlayer();
                }

                if (loweredVolumeDueToAudioFocus) {
                    loweredVolumeDueToAudioFocus = false;
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            // Damn, lost the audio focus for a (presumable) long time
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.w(TAG, "audiofocus loss");

                // Giving up everything
                //audioManager.unregisterMediaButtonEventReceiver(mediaButtonEventReceiver);
                //audioManager.abandonAudioFocus(this);

                //pausePlayer();
                stopMusicPlayer();
                break;

            // Just lost audio focus but will get it back shortly
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.w(TAG, "audiofocus loss transient");

                if (! isPaused()) {
                    pausePlayer();
                    pausedTemporarilyDueToAudioFocus = true;
                }
                break;

            // Temporarily lost audio focus but I can keep it playing
            // at a low volume instead of stopping completely
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.w(TAG, "audiofocus loss transient can duck");

                mediaPlayer .setVolume(0.1f, 0.1f);
                loweredVolumeDueToAudioFocus = true;
                break;
        }

        //TODO : look at this function and how everythngs works
    }
    // Internal flags for the function above {{
    private boolean pausedTemporarilyDueToAudioFocus = false;
    private boolean loweredVolumeDueToAudioFocus     = false;
    // }}

    /**
     * Called when the music is ready for playback.
     */
    @Override
    public void onPrepared(MediaPlayer mp) {

        //whats the use of mp ???
        serviceState = ServiceState.Playing;

        // Start playback
        mediaPlayer.start();

    }

    /**
     * Will be called when the music completes - either when the
     * user presses 'next' or when the music ends or when the user
     * selects another track.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {

        // Keep this state!
        serviceState = ServiceState.Playing;

        // TODO: Why do I need this?
/*		if (player.getCurrentPosition() <= 0)
			return;
*/
        broadcastState(MediaPlayerService.BROADCAST_EXTRA_COMPLETED);

        /* Repeating current song if desired
        if (repeatMode) {
            playSong();
            return;
        }

        // Remember that by calling next(), if played
        // the last song on the list, will reset to the
        // first one.
        next(false);

        // Reached the end, should we restart playing
        // from the first song or simply stop?
        if (currentSongPosition == 0) {
                playSong();

            //need to work on this .................

            return;
        } */
        // Common case - skipped a track or anything
        playSong();
    }


    /**
     * If something wrong happens with the MusicPlayer.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        Log.w(TAG, "onError");
        return false;
    }


    @Override
    public void onDestroy() {
        Context context = getApplicationContext();


            //left it, enble it in due time...
      //  currentSong = null;

        if (audioManager != null)
            audioManager.abandonAudioFocus(this);

        stopMusicPlayer();


        Log.w(TAG, "onDestroy");
        super.onDestroy();
    }

///////////////////////////////Binder///////////////////////////////////////

    /**
     * Tells if this Service is bound to an Activity
     */
    public boolean musicBound = false;

    /**
     * Defines the interaction between an Activity and this Service
     */
    public class MusicBinder extends Binder {

        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }


    /**
     * Token for the interaction between an Activity and this Service
     */
    private final IBinder musicBind = new MusicBinder();

    /**
     * Called when the service is bound to the app
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    /**
     * Called when the Service is unbound - user quitting
     * the app or something
     */
    @Override
    public boolean onUnbind(Intent intent){
        return  false;
    }

    //////////////////////////////End of Binder /////////////////////////////////


    /**
     * Jumps to the previous song on the list.
     *
     * @note Remember to call `playSong()` to make the MusicPlayer
     *       actually play the music.
     */
    public void previous(boolean userSkippedSong) {
//        if (serviceState != ServiceState.Paused && serviceState != ServiceState.Playing)
//            return;

        //Commented the line to check if it can work

        if (userSkippedSong) {

            broadcastState(MediaPlayerService.BROADCAST_EXTRA_SKIP_PREVIOUS);

        }

        currentSongPosition--;
        if (currentSongPosition < 0) {
            //Move to the top of the list....
            currentSongPosition = songs.size() - 1;
        }
    }

    /**
     * Jumps to the next song on the list.
     *
     * @note Remember to call `playSong()` to make the MusicPlayer
     *       actually play the music.
     */
    public void next(boolean userSkippedSong) {
        if (serviceState != ServiceState.Paused && serviceState != ServiceState.Playing)
            return;

        // TODO implement a queue of songs to prevent last songs
        //      to be played
        // TODO or maybe a playlist, whatever

        if (userSkippedSong)
            broadcastState(MediaPlayerService.BROADCAST_EXTRA_SKIP_NEXT);


       /* if (shuffleMode) {
            int newSongPosition = currentSongPosition;

            while (newSongPosition == currentSongPosition)
                newSongPosition = randomNumberGenerator.nextInt(songs.size());

            currentSongPosition = newSongPosition;
            return;
        }
        */

        currentSongPosition++;

        if (currentSongPosition >= songs.size()) {
            //Move to the bottom of the list
            currentSongPosition = 0;
        }
    }

    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        boolean returnValue = false;

        try {
            returnValue = mediaPlayer.isPlaying();
        }
        catch (IllegalStateException e) {
            mediaPlayer.reset();
            mediaPlayer.prepareAsync();
        }

        return returnValue;
    }

    public boolean isPaused() {
        return serviceState == ServiceState.Paused;
    }

    /**
     * Actually plays the song set by `currentSongPosition`.
     */
    public void playSong() {

        Log.w("tag", "reset media");
        mediaPlayer.reset();

        // Get the song ID from the list, extract the ID and
        // get an URL based on it
        Log.w("tag", "getting song _ID " + songs.size() );

        //scanning songs
        SongList songList = new SongList();
        songList.scanSongs(MediaPlayerService.this,"internl");
        MTA.songs = songList.songs;

         songToPlay = MTA.songs.get(currentSongPosition);

        Log.w("tag", "getting song _ID " + MTA.songs.size() );

        Log.w("tag", "current song -assignment");
        currentSong = songToPlay;

        Log.w("tag", "appending external URI");
        // Append the external URI with our songs'
        Uri songToPlayURI = ContentUris.withAppendedId
                (android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        songToPlay.getId()); //No internal ???? clever near ...lol (not funny )
        Log.w("tag", "_after");
        try {
            mediaPlayer.setDataSource(getApplicationContext(), songToPlayURI);
        }
        catch(IOException io) {
            Log.e(TAG, "IOException: couldn't change the song", io);
            destroySelf();
        }
        catch(Exception e) {
            Log.e(TAG, "Error when changing the song", e);
            destroySelf();
        }
        Log.w("tag", "prepare music asynchroo");

        // Prepare the MusicPlayer asynchronously.
        // When finished, will call `onPrepare`
        mediaPlayer.prepareAsync();
        serviceState = ServiceState.Preparing;

        broadcastState(MediaPlayerService.BROADCAST_EXTRA_PLAYING);


        Log.w(TAG, "play song");
    }


    //TODO :Read throw the two methods
    public void pausePlayer() {
        if (serviceState != ServiceState.Paused && serviceState != ServiceState.Playing)
            return;

        mediaPlayer.pause();
        serviceState = ServiceState.Paused;

        broadcastState(MediaPlayerService.BROADCAST_EXTRA_PAUSED);
    }

    public void unpausePlayer() {

//        if (serviceState != ServiceState.Paused && serviceState != ServiceState.Playing)
//            return;

        mediaPlayer.start();
        serviceState = ServiceState.Playing;

        broadcastState(MediaPlayerService.BROADCAST_EXTRA_UNPAUSED);
    }

    /**
     * Shouts the state of the Music Service.
     *
     * @note This broadcast is visible only inside this application.
     *
     * @note Will get received by listeners of `ServicePlayMusic.BROADCAST_ACTION`
     *
     * @param state Current state of the Music Service.
     */
    private void broadcastState(String state) {
        if (currentSong == null)
            return;

        Intent broadcastIntent = new Intent(MediaPlayerService.BROADCAST_ACTION);

        broadcastIntent.putExtra(MediaPlayerService.BROADCAST_EXTRA_STATE, state);
        broadcastIntent.putExtra(MediaPlayerService.BROADCAST_EXTRA_SONG_ID, currentSong.getId());

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(broadcastIntent);

        Log.w("TAG", "sentBroadcast");

        //TODO :Learn more about braodcast......
    }












    ///////////////////////////////// Destructors ///////////////////////////

    /**
     * Kills the service.
     *
     * @note Explicitly call this when the service is completed
     *       or whatnot.
     */
    private void destroySelf() {
        stopSelf();
        currentSong = null;
    }

    /**
     * Returns the song on the Now Playing List at `position`.
     */
    public Song getSong(int position) {
        return songs.get(position);
    }

    /**
     * Sets a specific song, already within internal Now Playing List.
     *
     * @param songIndex Index of the song inside the Now Playing List.
     */
    public void setSong(int songIndex) {

        if (songIndex < 0 || songIndex >= songs.size())
            currentSongPosition = 0;
        else
            currentSongPosition = songIndex;
    }



}
