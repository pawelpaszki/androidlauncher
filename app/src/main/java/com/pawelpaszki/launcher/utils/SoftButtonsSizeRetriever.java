package com.pawelpaszki.launcher.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by PawelPaszki on 27/06/2017.
 */

public class SoftButtonsSizeRetriever {

    public static int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }
}
