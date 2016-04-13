package com.pencorp.mta_ver_11.Activities.General;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.pencorp.mta_ver_11.Core.MTA;
import com.pencorp.mta_ver_11.Debuging_Tol.AdapterSong;
import com.pencorp.mta_ver_11.R;
import com.pencorp.mta_ver_11.Scanning_Songs.SongList;

public class MainActivity extends CoreActivity {

    private ListView songListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        songListView =(ListView)findViewById(R.id.list_view);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //Adding the adapter
        //Using the MTA arrayList
        SongList songList = new SongList();
        songList.scanSongs(MainActivity.this,"internal");
        MTA.songs=songList.songs;

        AdapterSong adapterSong = new AdapterSong(this,MTA.songs);
        songListView.setAdapter(adapterSong);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    //Helpet methods

    /**
     * Does another action on a thread
     *
     */
  /**  class ScanSongs extends AsyncTask<String, Integer ,String> {

     @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... params) {

            try {
                //will scan all songs  on the device

                return MainActivity.this.getString(R.string.menu_main_scanning_ok);
            }
            catch (Exception e){


                Log.e("Couldnt execute background task", e.toString());
                e.printStackTrace();
                return MainActivity.this.getString(R.string.menu_main_scanning_not_ok);
            }
        }

        /**
         * Called once the background processing is done.
         *
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            SingleToast.show(MainActivity.this,
                    result,
                    Toast.LENGTH_LONG);
        }
    } */
}



