
package com.pencorp.mta_ver_11.Activities.General;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pencorp.mta_ver_11.Core.MTA;

public class CoreActivity extends AppCompatActivity {

    public CoreActivity (){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mandatory - when creating we don't have
        // a theme applied yet.

        Log.w("tag", "starting service");

        MTA.startMusicService(this);

    }

}