package com.pawelpaszki.launcher.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by PawelPaszki on 09/05/2017.
 * Used to save/load icons from local storage
 */

public class IconLoader {

    public static void saveIcon(Context context, Bitmap bitmap, String name) {

        File directory = new File(context.getFilesDir().getAbsolutePath());

        File mypath=new File(directory, name + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos!= null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap loadImageFromStorage(String path, String name)
    {
        try {
            File f=new File(path, name + ".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }
}
