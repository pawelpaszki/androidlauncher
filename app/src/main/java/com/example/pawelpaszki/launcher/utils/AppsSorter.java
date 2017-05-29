package com.example.pawelpaszki.launcher.utils;

import android.content.Context;
import android.util.Log;

import com.example.pawelpaszki.launcher.AppDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by PawelPaszki on 23/05/2017.
 */

public class AppsSorter {

    public static List<AppDetail> sortApps(Context context, List<AppDetail> apps, String parameter, boolean isHomeActivity) {

        Log.i("sorting method", parameter);
        if(parameter.equals("name")) {
            Collections.sort(apps, new Comparator<AppDetail>() {
                @Override
                public int compare(AppDetail app1, AppDetail app2) {
                    return app1.getLabel().toString().compareTo(app2.getLabel().toString());
                }
            });
//            if(!isHomeActivity) {
//                if(SharedPrefs.getReverseListOrderFlag(context) == 1) {
//                    Collections.reverse(apps);
//                }
//            }
        } else {
            Collections.sort(apps, new NoOfStartsSorter() {
                @Override
                public int compare(AppDetail app1, AppDetail app2) {
                    if (app1.getNumberOfStarts() > app2.getNumberOfStarts())
                        return 1;
                    if (app1.getNumberOfStarts() < app2.getNumberOfStarts())
                        return -1;
                    return app1.getLabel().toString().compareTo(app2.getLabel().toString()) * -1;
                }
            });
                Collections.reverse(apps);
        }
        return apps;
    }
}
