package com.example.pawelpaszki.launcher.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;

import com.example.pawelpaszki.launcher.R;

/**
 * Created by PawelPaszki on 05/05/2017.
 */

public class BitMapFilter {

    public static Bitmap applyFilter(View v, Bitmap image) {
        image = addTexture(v, GrayScaleConverter.convertToGrayScale(image));
        return image;
    }

    public static Bitmap addTexture(View v, Bitmap image) {
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        Bitmap texture = BitmapFactory.decodeResource(v.getResources(), R.mipmap.texture);
        texture = Bitmap.createScaledBitmap(texture, image.getWidth(), image.getHeight(), false);
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                int p = texture.getPixel(x, y);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;

                int pp = image.getPixel(x, y);
                int r = (pp & 0xff0000) >> 16;
                int g = (pp & 0x00ff00) >> 8;
                int b = (pp & 0x0000ff) >> 0;
                if (!GrayScaleConverter.isTransparent(image, x, y) && R < 25 && G < 25 && B < 25) { //&& !(r < 25 && g < 25 && b < 25)
                    double d = Math.random();
                    //image.setPixel(x, y, manipulateColor(pp, 0.7f));
                    image.setPixel(x, y, 0x3949AB);
                }
            }
        }
        return image;
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
}
