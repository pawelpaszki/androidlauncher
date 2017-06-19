package com.example.pawelpaszki.launcher.utils;

import com.example.pawelpaszki.launcher.AppDetail;

import java.util.Comparator;

/**
 * Created by PawelPaszki on 10/05/2017.
 * Used to sort apps based on their start count
 */

class NoOfStartsSorter implements Comparator<AppDetail> {
    @Override
    public int compare(AppDetail app1, AppDetail app2) {
        int returnVal = 0;

        if(app1.getmNumberOfStarts() < app2.getmNumberOfStarts()){
            returnVal =  1;
        }else if(app1.getmNumberOfStarts() > app2.getmNumberOfStarts()){
            returnVal =  -1;
        }else if(app1.getmNumberOfStarts() == app2.getmNumberOfStarts()){
            returnVal =  0;
        }
        return returnVal;
    }
}
