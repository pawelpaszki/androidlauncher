package com.pawelpaszki.launcher.utils;

import android.content.Context;

import com.pawelpaszki.launcher.AppDetail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by PawelPaszki on 23/05/2017.
 */

public class AppsSorter {

    public static List<AppDetail> sortApps(List<AppDetail> apps, String parameter) {

        if(parameter.equals("name")) {
            Collections.sort(apps, new Comparator<AppDetail>() {
                @Override
                public int compare(AppDetail app1, AppDetail app2) {
                    return app1.getmLabel().toString().compareTo(app2.getmLabel().toString());
                }
            });

        } else {
            Collections.sort(apps, new NoOfStartsSorter() {
                @Override
                public int compare(AppDetail app1, AppDetail app2) {
                    if (app1.getmNumberOfStarts() > app2.getmNumberOfStarts())
                        return 1;
                    if (app1.getmNumberOfStarts() < app2.getmNumberOfStarts())
                        return -1;
                    return app1.getmLabel().toString().compareTo(app2.getmLabel().toString()) * -1;
                }
            });
                Collections.reverse(apps);
        }
        return apps;
    }
}
