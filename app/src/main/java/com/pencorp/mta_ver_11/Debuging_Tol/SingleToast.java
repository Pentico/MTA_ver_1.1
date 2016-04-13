package com.pencorp.mta_ver_11.Debuging_Tol;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Alfie on 2016/04/07.
 */
public class SingleToast {

    private static Toast singleToast = null;

    /**
     * Immediately shows a text message.
     * Use this the same way you would call `Toast`.
     *
     * @note It calls "show()" immediately.
     */
    public static void show (Context c, String text, int duration){

        if(singleToast != null){

            singleToast.cancel();

        }

        singleToast = Toast.makeText(c ,text,duration);
        singleToast.show();
    }
}
