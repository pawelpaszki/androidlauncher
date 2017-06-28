package com.pawelpaszki.launcher.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by PawelPaszki on 02/06/2017.
 * Used to save online icons and use them as custom app icons
 */

public class ImageDownloader extends AsyncTask<String,Void,Bitmap>{

    private Context mContext;
    private String mAppLabel;
    public ImageDownloader(Context mContext, String mAppLabel) {
        this.mContext = mContext;
        this.mAppLabel = mAppLabel;
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
        if(mAppLabel.equals("")) {
            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(mContext);
            try {
                myWallpaperManager.setBitmap(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            IconLoader.saveIcon(mContext, result, mAppLabel);
            SharedPrefs.setHomeReloadRequired(true, mContext);
            SharedPrefs.setNonDefaultIconsCount(SharedPrefs.getNonDefaultIconsCount(mContext) + 1,mContext);
        }
    }

}

