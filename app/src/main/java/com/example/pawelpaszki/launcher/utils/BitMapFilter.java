package com.example.pawelpaszki.launcher.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.example.pawelpaszki.launcher.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by PawelPaszki on 05/05/2017.
 */

public class BitMapFilter {

    public static Bitmap applyFilter(View v, Bitmap image) {
        image = addTexture(v.getResources(), GrayScaleConverter.convertToGrayScale(image));
        return image;
    }

    public static Bitmap addTexture(Resources resources, Bitmap image) {
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        Bitmap texture = BitmapFactory.decodeResource(resources, R.mipmap.texture);
        texture = Bitmap.createBitmap(texture, 0,0, image.getWidth(), image.getHeight());
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
                    //image.setPixel(x, y, 0x00000000);
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

    public static Bitmap getShadow(Resources resources, Bitmap image) {
        Bitmap texture = BitmapFactory.decodeResource(resources, R.mipmap.texture);

        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int textureHeight = texture.getHeight();
        int textureWidth = texture.getWidth();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        if(textureWidth > imageWidth) {
            Bitmap newImage = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
            for(int x = 0; x < textureWidth; x++) {
                for(int y = 0; y < textureHeight; y++) {
                    newImage.setPixel(x,y,android.graphics.Color.argb(0, 0, 0, 0));
                }
            }
            int heightDifference = (textureHeight - imageHeight) / 2;
            int widthDifference = (textureWidth - imageWidth) / 2;
            for(int x = 0; x < imageWidth; x++) {
                for(int y = 0; y < imageHeight; y++) {
                    newImage.setPixel(x + widthDifference, y + heightDifference, image.getPixel(x,y));
                }
            }
            image = newImage;
        }

        Bitmap shadowImage = GrayScaleConverter.convertToGrayScale(image);
        Bitmap.createScaledBitmap(shadowImage, image.getWidth(), image.getHeight()/6, false);

        Bitmap bgBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bgBitmap);
        canvas.drawARGB(63,255,255,255);
        for(int x = 0; x < bgBitmap.getWidth(); x++) {
            for(int y = 0; y < bgBitmap.getHeight() / 6; y++) {
                bgBitmap.setPixel(x, y + bgBitmap.getHeight() - shadowImage.getHeight(), shadowImage.getPixel(x,y));
            }
        }
        return bgBitmap;
    }

    public static Bitmap applyEdgeColors(Resources resources, Bitmap image) {
        Bitmap texture = BitmapFactory.decodeResource(resources, R.mipmap.texture);

        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int textureHeight = texture.getHeight();
        int textureWidth = texture.getWidth();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        if(textureWidth > imageWidth) {
            Bitmap newImage = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
            for(int x = 0; x < textureWidth; x++) {
                for(int y = 0; y < textureHeight; y++) {
                    newImage.setPixel(x,y,android.graphics.Color.argb(0, 0, 0, 0));
                }
            }
            int heightDifference = (textureHeight - imageHeight) / 2;
            int widthDifference = (textureWidth - imageWidth) / 2;
            for(int x = 0; x < imageWidth; x++) {
                for(int y = 0; y < imageHeight; y++) {
                    newImage.setPixel(x + widthDifference, y + heightDifference, image.getPixel(x,y));
                }
            }
            image = newImage;
        }


        // A1
        int a1 = 0;
        for (int x = 0; x < image.getWidth() - 8;) {
            for (int y = 0; y < image.getHeight() - 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    x++;
                    y++;
                } else {
                    int p = image.getPixel(x + 8, y + 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    a1 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(a1);
                    break;
                }
            }
        }

        // A2
        int a2 = 0;
        for (int x = 0; x < image.getWidth() - 8;) {
            for (int y = image.getHeight() / 3; y < image.getHeight();) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    if(x + 8 < image.getWidth()) {
                        x++;
                    } else {
                        break;
                    }
                } else {
                    int p = image.getPixel(x + 8, y);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    a2 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(a2);
                    break;
                }
            }
        }

        // A3
        int a3 = 0;
        for (int x = 0; x < image.getWidth() - 8;) {
            for (int y = image.getHeight() * 2 / 3; y < image.getHeight();) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    if(x + 8 < image.getWidth()) {
                        x++;
                    } else {
                        break;
                    }
                } else {
                    int p = image.getPixel(x + 8, y);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    a3 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(a3);
                    break;
                }
            }
        }

        // A4
        int a4 = 0;
        for (int x = 0; x < image.getWidth() - 8;) {
            for (int y = image.getHeight() - 1; y >= 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    x++;
                    y--;
                } else {
                    int p = image.getPixel(x + 8, y - 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    a4 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(a4);
                    break;
                }
            }
        }

        // b1
        int b1 = 0;
        for (int x = image.getWidth() / 3; x < image.getWidth();) {
            for (int y = 0; y < image.getHeight() - 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    y++;
                } else {
                    int p = image.getPixel(x, y + 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    b1 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(b1);
                    break;
                }
            }
        }

        // c1
        int c1 = 0;
        for (int x = image.getWidth() * 2 / 3; x < image.getWidth();) {
            for (int y = 0; y < image.getHeight() - 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    y++;
                } else {
                    int p = image.getPixel(x, y + 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    c1 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(c1);
                    break;
                }
            }
        }

        // b4
        int b4 = 0;
        for (int x = image.getWidth() / 3; x < image.getWidth();) {
            for (int y = image.getHeight() - 1; y >= 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    y--;
                } else {
                    int p = image.getPixel(x, y - 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    b4 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(b4);
                    break;
                }
            }
        }

        // c4
        int c4 = 0;
        for (int x = image.getWidth() * 2 / 3; x < image.getWidth();) {
            for (int y = image.getHeight() - 1; y >= 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    y--;
                } else {
                    int p = image.getPixel(x, y - 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    c4 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(c4);
                    break;
                }
            }
        }

        // D1
        int d1 = 0;
        for (int x = image.getWidth() - 1; x >= 8;) {
            for (int y = 0; y < image.getHeight() - 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    x--;
                    y++;
                } else {
                    int p = image.getPixel(x - 8, y + 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    d1 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(d1);
                    break;
                }
                if (y == image.getHeight()) {
                    break;
                }
            }
            if (x == image.getWidth()) {
                break;
            }
        }

        // D2
        int d2 = 0;
        for (int x = image.getWidth() - 1; x >= 8;) {
            for (int y = image.getHeight() / 3; y < image.getHeight();) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    if(x - 8 >= 0) {
                        x--;
                    } else {
                        break;
                    }
                } else {
                    int p = image.getPixel(x - 8, y);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    d2 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(d2);
                }
                if (y == image.getHeight()) {
                    break;
                }
            }
            if (x == image.getWidth()) {
                break;
            }
        }

        // D3
        int d3 = 0;
        for (int x = image.getWidth() - 1; x >= 8;) {
            for (int y = image.getHeight() * 2 / 3; y < image.getHeight();) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    if(x - 8 >= 0) {
                        x--;
                    } else {
                        break;
                    }
                } else {
                    int p = image.getPixel(x - 8, y);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    d3 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(d3);
                }
                if (y == image.getHeight()) {
                    break;
                }
            }
            if (x == image.getWidth()) {
                break;
            }
        }

        // D4
        int d4 = 0;
        for (int x = image.getWidth() - 1; x >= 8;) {
            for (int y = image.getHeight() - 1; y >= 8;) {
                if (GrayScaleConverter.isTransparent(image, x, y)) {
                    if(x - 8 >= 0) {
                        x--;
                    } else {
                        break;
                    }
                    if(y - 8 >= 0) {
                        y--;
                    } else {
                        break;
                    }
                } else {
                    int p = image.getPixel(x - 8, y - 8);
                    int A = Color.alpha(p);
                    int R = Color.red(p);
                    int G = Color.green(p);
                    int B = Color.blue(p);
                    d4 = android.graphics.Color.argb(A, R, G, B);
                    x = image.getWidth();
                    y = image.getHeight();
                    colors.add(d4);
                    break;
                }
                if (y == image.getHeight()) {
                    break;
                }
            }
            if (x == image.getWidth()) {
                break;
            }
        }



        int black = android.graphics.Color.argb(0, 0, 0, 0);
        // random pixels
//        int counter = 0;
//        Random random = new Random();
//        while(colors.size() < 32 || counter < 100) {
//            int x = random.nextInt(image.getWidth());
//            int y = random.nextInt(image.getHeight());
//            counter++;
//            if (GrayScaleConverter.isTransparent(image, x, y)) {
//                continue;
//            } else {
//                int p = image.getPixel(x - 8, y - 8);
//                int A = Color.alpha(p);
//                int R = Color.red(p);
//                int G = Color.green(p);
//                int B = Color.blue(p);
//                int color = android.graphics.Color.argb(A, R, G, B);
//                colors.add(color);
//            }
//        }
//        for (int x = 0; x < image.getWidth(); x++) {
//            for (int y = 0; y < image.getHeight(); y++) {
//                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
//                    int applyColor = random.nextInt(6);
//                    if(applyColor >=4) {
//                        image.setPixel(x, y, colors.get(random.nextInt(colors.size())));
//                    }
//
//                }
//            }
//        }

        // A1
        for (int x = 0; x < image.getWidth() / 4; x++) {
            for (int y = 0; y < image.getHeight() / 4; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, a1);
                }
            }
        }
        // A2
        for (int x = 0; x < image.getWidth() / 4; x++) {
            for (int y = image.getHeight() / 4; y < image.getHeight() / 2; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, a2);
                }
            }
        }
        // A3
        for (int x = 0; x < image.getWidth() / 4; x++) {
            for (int y = image.getHeight() / 2; y < image.getHeight() - (image.getHeight() / 4); y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, a3);
                }
            }
        }
        // A4
        for (int x = 0; x < image.getWidth() / 4; x++) {
            for (int y = image.getHeight() - (image.getHeight() / 4); y < image.getHeight(); y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, a4);
                }
            }
        }
        // B1
        for (int x = image.getWidth() / 4; x < image.getWidth() / 2; x++) {
            for (int y = 0; y < image.getHeight() / 4; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, b1);
                }
            }
        }
        // C1
        for (int x = image.getWidth() / 2; x < image.getWidth() - (image.getWidth() / 4); x++) {
            for (int y = 0; y < image.getHeight() / 4; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, c1);
                }
            }
        }
        // B4
        for (int x = image.getWidth() / 4; x < image.getWidth() / 2; x++) {
            for (int y = image.getHeight() - (image.getHeight() / 4); y < image.getHeight(); y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, b4);
                }
            }
        }
        // C4
        for (int x = image.getWidth() / 2; x < image.getWidth() - (image.getWidth() / 4); x++) {
            for (int y = image.getHeight() - (image.getHeight() / 4); y < image.getHeight(); y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, c4);
                }
            }
        }
        // D1
        for (int x = image.getWidth() - (image.getWidth() / 4); x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight() / 4; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, d1);
                }
            }
        }
        // D2
        for (int x = image.getWidth() - (image.getWidth() / 4); x < image.getWidth(); x++) {
            for (int y = image.getHeight() / 4; y < image.getHeight() / 2; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, d2);
                }
            }
        }
        // D3
        for (int x = image.getWidth() - (image.getWidth() / 4); x < image.getWidth(); x++) {
            for (int y = image.getHeight() / 2; y < image.getHeight() - image.getHeight() / 4; y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, d3);
                }
            }
        }
        // D4
        for (int x = image.getWidth() - (image.getWidth() / 4); x < image.getWidth(); x++) {
            for (int y = image.getHeight() - image.getHeight() / 4; y < image.getHeight(); y++) {
                if (GrayScaleConverter.isTransparent(image, x, y) && texture.getPixel(x, y) != black) {
                    image.setPixel(x, y, d4);
                }
            }
        }
        return image;
    }
}
