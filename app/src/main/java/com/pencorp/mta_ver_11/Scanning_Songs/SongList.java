
package com.pencorp.mta_ver_11.Scanning_Songs;



/**
 * Created by Alfie on 3/29/2016.
 */

        import android.content.ContentResolver;
        import android.content.Context;
        import android.database.Cursor;
        import android.net.Uri;
        import android.provider.MediaStore;

        import java.util.ArrayList;

/**
 * Global interface to all the songs this application can see
 */

public class SongList {

    /**
     * List of all Songs
     */
    public  ArrayList<Song> songs = new ArrayList<Song>();  //why not static ??

    /**
     * Flag that tells if we're  scanning songs right now
     */
    private boolean scanningSongs;

    /**
     * Flag that tells if successfully scanned all songs.
     */
    private boolean scannedSongs;

    /**
     * Tells if we've successfully scanned all songs on
     * the device.
     *
     * This will return `false` both while we're scanning
     * for songs and if some error happened while scanning.
     */
    public boolean isInitialized() {
        return scannedSongs;
    }

    /**
     * Tells if we're currently scanning songs on the device.
     */
    public boolean isScanning() {
        return scanningSongs;
    }


    /**
     * This is going to be long
     */
    public void scanSongs(Context c,String fromWhere){

        if(scanningSongs)
            return;

        Uri musicUri = ((fromWhere == "internal") ?
                android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI:
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

        //Gives us access to query for files on the system.
        ContentResolver resolver = c.getContentResolver();

        //Iterate throught the results of a SQLite database query.
        Cursor cursor;

        // These are the columns from the system databases.
        // They're the information I want to get from songs.
        String GENRE_ID      = MediaStore.Audio.Genres._ID;
        String GENRE_NAME    = MediaStore.Audio.Genres.NAME;
        String SONG_ID       = android.provider.MediaStore.Audio.Media._ID;
        String SONG_TITLE    = android.provider.MediaStore.Audio.Media.TITLE;
        String SONG_ARTIST   = android.provider.MediaStore.Audio.Media.ARTIST;
        String SONG_ALBUM    = android.provider.MediaStore.Audio.Media.ALBUM;
        String SONG_YEAR     = android.provider.MediaStore.Audio.Media.YEAR;
        String SONG_TRACK_NO = android.provider.MediaStore.Audio.Media.TRACK;
        String SONG_FILEPATH = android.provider.MediaStore.Audio.Media.DATA;
        String SONG_DURATION = android.provider.MediaStore.Audio.Media.DURATION;

        // Columns I'll retrieve from the song table
        String[] columns = {
                SONG_ID,
                SONG_TITLE,
                SONG_ARTIST,
                SONG_ALBUM,
                SONG_YEAR,
                SONG_TRACK_NO,
                SONG_FILEPATH,
                SONG_DURATION
        };

        // Filters the results to show music files
        final String musicsOnly = MediaStore.Audio.Media.IS_MUSIC + "=1";

        // Actually querying the system
        cursor = resolver.query(musicUri, columns, musicsOnly, null, null);

        if(cursor != null && cursor.moveToFirst()){

            do{
                // Creating a song from the values on the row
                Song song = new Song(cursor.getInt(cursor.getColumnIndex(SONG_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_FILEPATH)));

                song.setTitle      (cursor.getString(cursor.getColumnIndex(SONG_TITLE)));
                song.setArtist     (cursor.getString(cursor.getColumnIndex(SONG_ARTIST)));
                song.setAlbum      (cursor.getString(cursor.getColumnIndex(SONG_ALBUM)));
                song.setYear       (cursor.getInt   (cursor.getColumnIndex(SONG_YEAR)));
                song.setTrackNumber(cursor.getInt   (cursor.getColumnIndex(SONG_TRACK_NO)));
                song.setDuration   (cursor.getInt   (cursor.getColumnIndex(SONG_DURATION)));

                songs.add(song);
            }while (cursor.moveToNext());
        }else{
            //What Happens with no songs? :(
        }
        cursor.close();


        scannedSongs  = true;
        scanningSongs = false;


    }
}
