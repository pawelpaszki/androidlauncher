package com.example.pawelpaszki.launcher.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by PawelPaszki on 05/05/2017.
 */

public class GrayScaleConverter {

    public static Bitmap convertToGrayScale(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Log.i("config", image.getConfig().toString());
        int pp = image.getPixel(77, 77);

        ///
        int AA = Color.alpha(pp);
        int RR = Color.red(pp);
        int GG = Color.green(pp);
        int BB = Color.blue(pp);
        Log.i("r,g,b,a", AA + ":" + RR + ":" + GG + ":" + BB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isTransparent(image,x, y)) {
                    Bitmap newImage = image;
                    Color c = new Color();

                    int p = newImage.getPixel(x, y);

                    ///
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    ////

                    int red = (int) (R * 0.21);
                    int green = (int) (G * 0.72);
                    int blue = (int) (B * 0.07);
                    int sum = red + green + blue;
                    int RGB = android.graphics.Color.argb(A, sum, sum, sum);
                    newImage.setPixel(x, y, RGB);
                }
            }
        }
        return darkenBitMap(image);
    }

    private static Bitmap darkenBitMap(Bitmap bm) {

        Canvas canvas = new Canvas(bm);
        Paint p = new Paint(Color.RED);
        //ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222); // lighten
        ColorFilter filter = new LightingColorFilter(0xFF262626, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return bm;
    }

    public static boolean isTransparent(Bitmap image, int x, int y) {
        int pixel = image.getPixel(x, y);
        if ((pixel >> 24) == 0x00) {
            return true;
        } else {
            return false;
        }
    }
}
