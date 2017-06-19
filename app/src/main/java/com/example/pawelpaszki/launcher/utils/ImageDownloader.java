package com.example.pawelpaszki.launcher.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by PawelPaszki on 02/06/2017.
 * Used to save online icons and use them as custom app icons
 */

public class ImageDownloader extends AsyncTask<String,Void,Bitmap>{

    private Context context;
    private String appLabel;
    public ImageDownloader(Context context, String appLabel) {
        this.context = context;
        this.appLabel = appLabel;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        InputStream in;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Bitmap result) {
        Log.i("result size", String.valueOf(result.getHeight()));
        Log.i("result name", appLabel);
        IconLoader.saveIcon(context, result, appLabel);
        SharedPrefs.setHomeReloadRequired(true, context);
        SharedPrefs.setNonDefaultIconsCount(SharedPrefs.getNonDefaultIconsCount(context) + 1,context);
    }

}

